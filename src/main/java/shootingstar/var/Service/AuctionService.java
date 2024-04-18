package shootingstar.var.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shootingstar.var.dto.req.AuctionReportReqDto;
import shootingstar.var.entity.auction.Auction;
import shootingstar.var.entity.auction.AuctionReport;
import shootingstar.var.entity.log.PointLog;
import shootingstar.var.enums.type.AuctionType;
import shootingstar.var.entity.ScheduledTask;
import shootingstar.var.enums.type.PointOriginType;
import shootingstar.var.enums.type.TaskType;
import shootingstar.var.entity.User;
import shootingstar.var.enums.type.UserType;
import shootingstar.var.exception.CustomException;
import shootingstar.var.exception.ErrorCode;
import shootingstar.var.repository.AuctionReportRepository;
import shootingstar.var.scheduling.quartz.TicketCreationJob;
import shootingstar.var.repository.AuctionRepository;
import shootingstar.var.repository.log.PointLogRepository;
import shootingstar.var.repository.ScheduledTaskRepository;
import shootingstar.var.repository.user.UserRepository;
import shootingstar.var.dto.req.AuctionCreateReqDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

    private final String JOB_GROUP_NAME = "ticket-creation-jobs";

    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final PointLogRepository pointLogRepository;
    private final ScheduledTaskRepository scheduledTaskRepository;
    private final AuctionReportRepository auctionReportRepository;
    private final Scheduler scheduler;

    @Transactional
    public void create(AuctionCreateReqDto reqDto, String userUUID) {
        User findUser = userRepository.findByUserUUIDWithPessimisticLock(userUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        validateMinBidAmount(reqDto, findUser);

        LocalDateTime auctionCloseTime = LocalDateTime.now().plusDays(3);

        // 경매 생성
        Auction auction = Auction.builder()
                .user(findUser)
                .minBidAmount(reqDto.getMinBidAmount())
                .meetingDate(LocalDateTime.parse(reqDto.getMeetingDate()))
                .meetingLocation(reqDto.getMeetingLocation())
                .meetingInfoText(reqDto.getMeetingInfoText())
                .meetingPromiseText(reqDto.getMeetingPromiseText())
                .meetingInfoImg(reqDto.getMeetingInfoImg())
                .meetingPromiseImg(reqDto.getMeetingPromiseImg())
                .auctionCloseTime(auctionCloseTime)
                .build();

        auctionRepository.save(auction);

        // 포인트 차감
        log.info("사용자 감소 전 포인트 : {}", findUser.getPoint());
        findUser.decreasePoint(BigDecimal.valueOf(auction.getMinBidAmount()));
        log.info("사용자 감소 후 포인트 : {}", findUser.getPoint());

        PointLog pointLog = PointLog.createPointLogWithWithdrawal(findUser, PointOriginType.AUCTION_REGISTRATION_DEPOSIT, BigDecimal.valueOf(auction.getMinBidAmount()));
        pointLogRepository.save(pointLog);

        // 스케줄링 저장
        schedulingCreateTicket(auction, findUser);
    }

    private void validateMinBidAmount(AuctionCreateReqDto reqDto, User findUser) {
        // 최소 입찰 금액이 10000원으로 나누어 떨어지지 않을 경우
        if (reqDto.getMinBidAmount() % 10000 != 0) {
            throw new CustomException(ErrorCode.INCORRECT_FORMAT_MIN_BID_AMOUNT);
        }

        // 보유 포인트보다 최소 입찰 금액이 더 클 경우
        if (findUser.getPoint().compareTo(BigDecimal.valueOf(reqDto.getMinBidAmount())) == -1) {
            throw new CustomException(ErrorCode.MIN_BID_AMOUNT_INCORRECT_FORMAT);
        }
    }

    public void schedulingCreateTicket(Auction auction, User findUser) {
        LocalDateTime scheduleTime = auction.getAuctionCloseTime();
        ScheduledTask task = ScheduledTask.builder()
                .auctionId(auction.getAuctionId())
                .userId(auction.getUser().getUserId())
                .scheduledTime(scheduleTime)
                .build();
        scheduledTaskRepository.save(task);

        JobDetail jobDetail = JobBuilder.newJob(TicketCreationJob.class)
                .withIdentity(auction.getAuctionUUID() + "-" + task.getScheduledTaskId(), JOB_GROUP_NAME)
                .usingJobData("auctionId", auction.getAuctionId())
                .usingJobData("userId", findUser.getUserId())
                .usingJobData("scheduledTaskId", task.getScheduledTaskId())
                .build();

        Instant instant = scheduleTime.atZone(ZoneId.systemDefault()).toInstant();
        String TRIGGER_GROUP_NAME = "ticket-creation-triggers";
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobDetail.getKey().getName(), TRIGGER_GROUP_NAME)
                .startAt(Date.from(instant))
                .build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("스케쥴링 추가");
        } catch (SchedulerException e) {
            throw new CustomException(ErrorCode.SCHEDULING_SERVER_ERROR);
        }
    }

    @Transactional
    public void cancel(String auctionUUID, String userUUID, String userType) {
        // uuid에 해당하는 경매가 존재하는지 확인
        Auction findAuction = findAuctionByAuctionUUID(auctionUUID);

        // 찾은 경매가 로그인 한 유저가 생성했거나 유저 권한이 ADMIN인지 확인
        checkAuctionCancelAccess(userUUID, userType, findAuction);

        // 경매 타입이 PROGRESS인지 확인
        validateAuctionType(findAuction);

        // 경매 타입을 CANCEL로 변경
        findAuction.changeAuctionType(AuctionType.CANCEL);
        log.info("경매가 취소되었습니다. auctionUUID : {}", findAuction.getAuctionUUID());

        PointOriginType pointOriginType = userType.equals(UserType.ROLE_VIP.toString()) ? PointOriginType.VIP_AUCTION_CANCEL : PointOriginType.ADMIN_AUCTION_CANCEL;

        // 입찰에 참여한 유저가 있을 때, 현재 최고 입찰자에게 현재 최고 입찰 금액 반환
        refundHighestBidderOnAuctionCancellation(findAuction, pointOriginType);

        // 사용자 포인트에 += 최소입찰금액
        refundDepositToOrganizer(findAuction, pointOriginType);

        deleteScheduling(findAuction);
    }

    private void checkAuctionCancelAccess(String userUUID, String userType, Auction auction) {
        if (!auction.isOwner(userUUID) && !userType.equals(UserType.ROLE_ADMIN.toString())) {
            throw new CustomException(ErrorCode.AUCTION_ACCESS_DENIED);
        }
    }

    public void refundHighestBidderOnAuctionCancellation(Auction findAuction, PointOriginType pointOriginType) {
        if (findAuction.getCurrentHighestBidderUUID() != null) {
            User findCurrentHighestBidder = userRepository.findByUserUUIDWithPessimisticLock(findAuction.getCurrentHighestBidderUUID())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            log.info("경매 취소로 인해 최고 입찰자에게 포인트가 반환될 예정입니다.");
            log.info("최고입찰자 uuid : {}, 추가 전 포인트 : {}", findCurrentHighestBidder.getUserUUID(), findCurrentHighestBidder.getPoint());

            findCurrentHighestBidder.increasePoint(BigDecimal.valueOf(findAuction.getCurrentHighestBidAmount()));

            log.info("최고입찰자 uuid : {}, 추가 후 포인트 : {}", findCurrentHighestBidder.getUserUUID(), findCurrentHighestBidder.getPoint());

            PointLog pointLog = PointLog.createPointLogWithDeposit(findCurrentHighestBidder,
                    pointOriginType, BigDecimal.valueOf(findAuction.getCurrentHighestBidAmount()));
            pointLogRepository.save(pointLog);
        }
    }

    public void refundDepositToOrganizer(Auction findAuction, PointOriginType pointOriginType) {
        User organizer = userRepository.findByUserUUIDWithPessimisticLock(findAuction.getUser().getUserUUID())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        log.info("경매 취소로 인해 경매 주최자에게 포인트가 반환될 예정입니다.");
        log.info("경매 주최자 id : {}, 추가 전 포인트 : {}", organizer.getUserId(), organizer.getPoint());
        organizer.increasePoint(BigDecimal.valueOf(findAuction.getMinBidAmount()));
        log.info("경매 주최자 id : {}, 추가 후 포인트 : {}", organizer.getUserId(), organizer.getPoint());

        PointLog pointLog = PointLog.createPointLogWithDeposit(organizer, pointOriginType, BigDecimal.valueOf(findAuction.getMinBidAmount()));
        pointLogRepository.save(pointLog);
    }

    public void deleteScheduling(Auction findAuction) {
        ScheduledTask task = scheduledTaskRepository.findByAuctionId(findAuction.getAuctionId())
                .orElseThrow(() -> new CustomException(ErrorCode.TASK_NOT_FOUND));

        // 예정된 스케줄링 작업 삭제
        JobKey jobKey = new JobKey(findAuction.getAuctionUUID() + "-" + task.getScheduledTaskId(), JOB_GROUP_NAME);

        try {
            boolean isDeleted = scheduler.deleteJob(jobKey);
            if (!isDeleted) {
                throw new CustomException(ErrorCode.FAIL_TASK_DELETE);
            }
        } catch (SchedulerException e) {
            log.info("스케줄러 취소 에러", e);
            throw new CustomException(ErrorCode.SCHEDULING_SERVER_ERROR);
        }

        // 스케줄링 타입 CANCEL로 변경
        task.changeTaskType(TaskType.CANCEL);
    }

    @Transactional
    public void reportAuction(AuctionReportReqDto reqDto, String userUUID) {
        // 경매 존재 여부
        Auction auction = findAuctionByAuctionUUID(reqDto.getAuctionUUID());

        // 경매 타입이 PROGRESS인지 확인
        validateAuctionType(auction);

        User user = userRepository.findByUserUUID(userUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        AuctionReport auctionReport = AuctionReport.builder()
                .auction(auction)
                .auctionReportNickname(user.getNickname())
                .auctionReportContent(reqDto.getAuctionReportContent())
                .build();

        auctionReportRepository.save(auctionReport);
    }

    private Auction findAuctionByAuctionUUID(String auctionUUID) {
        Auction findAuction = auctionRepository.findByAuctionUUID(auctionUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
        return findAuction;
    }

    private void validateAuctionType(Auction findAuction) {
        if (!findAuction.isProgress()) {
            throw new CustomException(ErrorCode.AUCTION_CONFLICT);
        }
    }
}

