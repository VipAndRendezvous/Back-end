package shootingstar.var.Service;

import java.math.BigDecimal;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shootingstar.var.dto.req.BidReqDto;
import shootingstar.var.dto.res.BidResDto;
import shootingstar.var.entity.Auction;
import shootingstar.var.entity.Bid;
import shootingstar.var.entity.PointLog;
import shootingstar.var.entity.User;
import shootingstar.var.enums.type.PointOriginType;
import shootingstar.var.exception.CustomException;
import shootingstar.var.exception.ErrorCode;
import shootingstar.var.repository.AuctionRepository;
import shootingstar.var.repository.BidRepository;
import shootingstar.var.repository.PointLogRepository;
import shootingstar.var.repository.user.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidService {
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;
    private final PointLogRepository pointLogRepository;

    @Transactional
    public BidResDto participateAuction(String userUUID, BidReqDto bidDto) {
        Auction auction = auctionRepository.findByAuctionUUIDWithPessimisticLock(bidDto.getAuctionUUID())
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));

        // 진행중인 경매인지 확인
        validateAuctionType(auction);

        // 입찰하는 사용자가 경매 주최자인지 확인
        if (auction.getUser().getUserUUID().equals(userUUID)) {
            throw new CustomException(ErrorCode.AUCTION_ACCESS_DENIED);
        }

        // 입력된 입찰 금액이 이전 최고 입찰 금액보다 크면서 경매호가표 기준의 응찰가가 맞는지 확인
        validateCurrentHighestBidAmount(bidDto, auction);

        User currentUser = userRepository.findByUserUUIDWithPessimisticLock(userUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 사용자의 포인트가 입찰 금액보다 적은지 확인
        validateSufficientPointForBid(bidDto, currentUser);

        PointLog pointLog;
        // 이전 최고 입찰자에게 포인트 반환
        if (auction.getCurrentHighestBidderUUID() != null) {
            User beforeHighestBidder = userRepository.findByUserUUIDWithPessimisticLock(auction.getCurrentHighestBidderUUID())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            log.info("이전 최고 입찰자의 반환 전 포인트 : {}", beforeHighestBidder.getPoint());
            beforeHighestBidder.increasePoint(BigDecimal.valueOf(auction.getCurrentHighestBidAmount()));
            log.info("이전 최고 입찰자의 반환 후 포인트 : {}", beforeHighestBidder.getPoint());

            // 포인트 로그 로직 필요
            pointLog = PointLog.createPointLogWithDeposit(beforeHighestBidder, PointOriginType.BID, BigDecimal.valueOf(auction.getCurrentHighestBidAmount()));
            pointLogRepository.save(pointLog);
        }

        // 사용자의 포인트 차감
        log.info("현재 최고 입찰자의 차감 전 포인트 : {}", currentUser.getPoint());
        currentUser.decreasePoint(BigDecimal.valueOf(bidDto.getPrice()));
        log.info("현재 최고 입찰자의 차감 후 포인트 : {}", currentUser.getPoint());

        // 포인트 로그 로직 필요
        pointLog = PointLog.createPointLogWithWithdrawal(currentUser, PointOriginType.BID,
                BigDecimal.valueOf(bidDto.getPrice()));
        pointLogRepository.save(pointLog);

        // 경매 입찰 수, 현재 최고 입찰자, 최고 입찰 금액 변경
        auction.increaseBidCount();
        auction.changeCurrentHighestBidderUUID(userUUID);
        auction.changeCurrentHighestBidAmount(bidDto.getPrice());

        // 입찰 정보 저장
        Bid bid = Bid.builder()
                .auction(auction)
                .bidderNickname(currentUser.getNickname())
                .bidAmount(bidDto.getPrice())
                .build();
        bidRepository.save(bid);

        return BidResDto.builder()
                .currentHighestBidderNickname(currentUser.getNickname())
                .currentHighestBidAmount(bidDto.getPrice())
                .userPoint(currentUser.getPoint())
                .build();
    }

    private void validateAuctionType(Auction findAuction) {
        if (!findAuction.isProgress()) {
            throw new CustomException(ErrorCode.AUCTION_CONFLICT);
        }
    }

    private void validateCurrentHighestBidAmount(BidReqDto bidDto, Auction auction) {
        if ((auction.getCurrentHighestBidAmount() == 0 && auction.getMinBidAmount() > bidDto.getPrice())
                || (auction.getCurrentHighestBidAmount() != 0 && auction.getCurrentHighestBidAmount() >= bidDto.getPrice())) {
            throw new CustomException(ErrorCode.INCORRECT_FORMAT_PRICE);
        }

        long currentPrice;
        if (auction.getCurrentHighestBidAmount() == 0) {
            currentPrice = auction.getMinBidAmount();
        } else {
            currentPrice = auction.getCurrentHighestBidAmount();
        }

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
}