package shootingstar.var.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shootingstar.var.dto.req.BidReqDto;
import shootingstar.var.dto.res.BidInfoResDto;
import shootingstar.var.dto.res.BidLog;
import shootingstar.var.dto.res.BidResDto;
import shootingstar.var.entity.ScheduledTask;
import shootingstar.var.entity.auction.Auction;
import shootingstar.var.entity.Bid;
import shootingstar.var.entity.log.PointLog;
import shootingstar.var.entity.User;
import shootingstar.var.enums.type.PointOriginType;
import shootingstar.var.exception.CustomException;
import shootingstar.var.exception.ErrorCode;
import shootingstar.var.repository.AuctionRepository;
import shootingstar.var.repository.BidRepository;
import shootingstar.var.repository.ScheduledTaskRepository;
import shootingstar.var.repository.log.PointLogRepository;
import shootingstar.var.repository.user.UserRepository;
import shootingstar.var.util.ParticipatingAuctionRedisUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidService {
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;
    private final PointLogRepository pointLogRepository;
    private final ScheduledTaskRepository scheduledTaskRepository;
    private final Scheduler scheduler;
    private final ParticipatingAuctionRedisUtil participatingAuctionRedisUtil;

    @Transactional
    public BidResDto participateAuction(String userUUID, BidReqDto bidDto) {
        Auction auction = auctionRepository.findByAuctionUUIDWithPessimisticLock(bidDto.getAuctionUUID())
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));

        // 진행중인 경매인지 확인
        validateAuctionType(auction);

        // 입찰하는 사용자가 경매 주최자인지 확인
        validateUserIsOrganizer(userUUID, auction);

        // 이전 최고 입찰자가 현재 입찰하는 사용자일 때
        validateUserIsCurrentHighestBidder(userUUID, auction);

        // 입력된 입찰 금액이 이전 최고 입찰 금액보다 크면서 경매호가표 기준의 응찰가가 맞는지 확인
        validateCurrentHighestBidAmount(bidDto, auction);

        User currentUser = userRepository.findByUserUUIDWithPessimisticLock(userUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 입찰하는 사용자가 구독하고 있는 상태인지 확인
        currentUser.validateSubscribeExpiration();

        // 사용자의 포인트가 입찰 금액보다 적은지 확인
        validateSufficientPointForBid(bidDto, currentUser);

        // 마감 30분 전 응찰한다면 마감 시간 5분 증가
        increaseEndTime(auction);

        // 이전 최고 입찰자에게 포인트 반환
        refundHighestBidderOnBid(auction);

        // 사용자의 포인트 차감
        subtractUserPointsForBid(bidDto, currentUser);

        // 경매 입찰 수, 현재 최고 입찰자, 최고 입찰 금액 변경
        updateAuctionInfo(userUUID, bidDto, auction);

        // 입찰 정보 저장
        Bid bid = Bid.builder()
                .auction(auction)
                .bidderNickname(currentUser.getNickname())
                .bidAmount(bidDto.getPrice())
                .build();
        bidRepository.save(bid);

        // 레디스에 추가
        LocalDateTime localDateTime = auction.getAuctionCloseTime();
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        participatingAuctionRedisUtil.addParticipation(currentUser.getUserUUID(), auction.getAuctionUUID(), instant.toEpochMilli());

        return BidResDto.builder()
                .currentHighestBidderNickname(currentUser.getNickname())
                .currentHighestBidAmount(bidDto.getPrice())
                .build();
    }

    protected void validateUserIsOrganizer(String userUUID, Auction auction) {
        if (auction.getUser().getUserUUID().equals(userUUID)) {
            throw new CustomException(ErrorCode.AUCTION_ACCESS_DENIED);
        }
    }

    protected void validateUserIsCurrentHighestBidder(String userUUID, Auction auction) {
        if (auction.getCurrentHighestBidderUUID() != null && auction.getCurrentHighestBidderUUID().equals(userUUID)) {
            throw new CustomException(ErrorCode.AUCTION_ACCESS_DENIED);
        }
    }

    private void validateCurrentHighestBidAmount(BidReqDto bidDto, Auction auction) {
        if (auction.getBidCount() == 0) {
            if (auction.getMinBidAmount() != bidDto.getPrice()) {
                throw new CustomException(ErrorCode.INCORRECT_FORMAT_PRICE);
            }
            return;
        }

        long currentPrice = auction.getCurrentHighestBidAmount();

        Entry<Long, Long> entry = getBidIncrementForCurrentPrice(auction, currentPrice);
        log.info("bidDto.getPrice() : {}", bidDto.getPrice());
        log.info("currentPrice : {}", currentPrice);
        log.info("entry.getValue(), {}", entry.getValue());
        if (entry != null && bidDto.getPrice() != currentPrice + entry.getValue()) {
            throw new CustomException(ErrorCode.INCORRECT_FORMAT_PRICE);
        }
    }

    private Entry<Long, Long> getBidIncrementForCurrentPrice(Auction auction, long currentPrice) {
        NavigableMap<Long, Long> bidIncrementRules = new TreeMap<>();
        bidIncrementRules.put(auction.getMinBidAmount(), 50_000L); // 최소 입찰가 부터 시작
        bidIncrementRules.put(1_000_000L, 100_000L);
        bidIncrementRules.put(3_000_000L, 200_000L);
        bidIncrementRules.put(5_000_000L, 300_000L);
        bidIncrementRules.put(10_000_000L, 500_000L);
        bidIncrementRules.put(30_000_000L, 1_000_000L);
        bidIncrementRules.put(50_000_000L, 2_000_000L);
        bidIncrementRules.put(100_000_000L, 3_000_000L);
        bidIncrementRules.put(200_000_000L, 5_000_000L);

        Entry<Long, Long> entry = bidIncrementRules.floorEntry(currentPrice);
        return entry;
    }

    private void validateSufficientPointForBid(BidReqDto bidDto, User currentUser) {
        if (currentUser.getPoint().subtract(BigDecimal.valueOf(bidDto.getPrice())).compareTo(BigDecimal.ZERO) == -1) {
            throw new CustomException(ErrorCode.INCORRECT_FORMAT_PRICE);
        }
    }

    public void increaseEndTime(Auction auction) {
        if (!auction.isExtended() &&
                LocalDateTime.now().isAfter(auction.getAuctionCloseTime().minusMinutes(30))) {

            LocalDateTime closeTime = auction.getAuctionCloseTime().plusMinutes(5);

            log.info("응찰 시간 연장");
            auction.changeIsExtended(true);
            auction.changeAuctionCloseTime(closeTime);

            ScheduledTask task = scheduledTaskRepository.findByAuctionId(auction.getAuctionId())
                    .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));

            task.changeScheduledTime(closeTime);

            // 예정된 스케줄링 작업 수정
            try {
                String TRIGGER_GROUP_NAME = "ticket-creation-triggers";
                TriggerKey triggerKey = new TriggerKey(auction.getAuctionUUID() + "-" + task.getScheduledTaskId(), TRIGGER_GROUP_NAME);
                Trigger oldTrigger = scheduler.getTrigger(triggerKey);

                Instant instant = closeTime.atZone(ZoneId.systemDefault()).toInstant();
                Trigger newTrigger = oldTrigger.getTriggerBuilder()
                        .startAt(Date.from(instant))
                        .build();

                scheduler.rescheduleJob(triggerKey, newTrigger);

            } catch (SchedulerException e) {
                log.info("마감 30분전 스케줄링 변경 에러", e);
                throw new CustomException(ErrorCode.SCHEDULING_SERVER_ERROR);
            }
        }
    }

    public void refundHighestBidderOnBid(Auction auction) {
        PointLog pointLog;
        if (auction.getCurrentHighestBidderUUID() != null) {
            User beforeHighestBidder = userRepository.findByUserUUIDWithPessimisticLock(auction.getCurrentHighestBidderUUID())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            log.info("이전 최고 입찰자의 반환 전 포인트 : {}", beforeHighestBidder.getPoint());
            beforeHighestBidder.increasePoint(BigDecimal.valueOf(auction.getCurrentHighestBidAmount()));
            log.info("이전 최고 입찰자의 반환 후 포인트 : {}", beforeHighestBidder.getPoint());

            // 포인트 로그
            pointLog = PointLog.createPointLogWithDeposit(beforeHighestBidder, PointOriginType.BID, BigDecimal.valueOf(
                    auction.getCurrentHighestBidAmount()));
            pointLogRepository.save(pointLog);
        }
    }

    public void subtractUserPointsForBid(BidReqDto bidDto, User currentUser) {
        PointLog pointLog;
        log.info("현재 최고 입찰자의 차감 전 포인트 : {}", currentUser.getPoint());
        currentUser.decreasePoint(BigDecimal.valueOf(bidDto.getPrice()));
        log.info("현재 최고 입찰자의 차감 후 포인트 : {}", currentUser.getPoint());

        // 포인트 로그
        pointLog = PointLog.createPointLogWithWithdrawal(currentUser, PointOriginType.BID,
                BigDecimal.valueOf(bidDto.getPrice()));
        pointLogRepository.save(pointLog);
    }

    public void updateAuctionInfo(String userUUID, BidReqDto bidDto, Auction auction) {
        auction.increaseBidCount();
        auction.changeCurrentHighestBidderUUID(userUUID);
        auction.changeCurrentHighestBidAmount(bidDto.getPrice());
    }

    public BidInfoResDto findBidInfo(String auctionUUID) {
        Auction auction = auctionRepository.findByAuctionUUID(auctionUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));

        // 진행중인 경매인지 확인
        validateAuctionType(auction);

        List<Bid> bids = bidRepository.findTop10ByAuction_AuctionIdOrderByCreatedTimeDesc(
                auction.getAuctionId());

        List<BidLog> bidLogs = bids.stream()
                .map(bid -> BidLog.builder()
                        .bidderNickname(bid.getBidderNickname())
                        .bidAmount(bid.getBidAmount())
                        .participatedBidTime(bid.getCreatedTime())
                        .build())
                .collect(Collectors.toList());

        return BidInfoResDto.builder()
                .currentHighestBidAmount(auction.getCurrentHighestBidAmount())
                .bidLogs(bidLogs)
                .build();
    }

    protected void validateAuctionType(Auction findAuction) {
        if (!findAuction.isProgress()) {
            throw new CustomException(ErrorCode.AUCTION_CONFLICT);
        }
    }
}
