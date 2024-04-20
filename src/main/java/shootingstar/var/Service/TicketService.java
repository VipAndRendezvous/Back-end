package shootingstar.var.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shootingstar.var.dto.req.MeetingTimeSaveReqDto;
import shootingstar.var.dto.req.ReviewSaveReqDto;
import shootingstar.var.dto.req.TicketReportReqDto;
import shootingstar.var.dto.res.DetailTicketResDto;
import shootingstar.var.entity.auction.Auction;
import shootingstar.var.entity.log.PointLog;
import shootingstar.var.entity.Review;
import shootingstar.var.entity.ticket.Ticket;
import shootingstar.var.entity.ticket.TicketMeetingTime;
import shootingstar.var.entity.ticket.TicketReport;
import shootingstar.var.entity.User;
import shootingstar.var.enums.type.PointOriginType;
import shootingstar.var.exception.CustomException;
import shootingstar.var.exception.ErrorCode;
import shootingstar.var.repository.log.PointLogRepository;
import shootingstar.var.repository.review.ReviewRepository;
import shootingstar.var.repository.ticket.TicketMeetingTimeRepository;
import shootingstar.var.repository.ticket.TicketReportRepository;
import shootingstar.var.repository.ticket.TicketRepository;
import shootingstar.var.repository.user.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketMeetingTimeRepository ticketMeetingTimeRepository;
    private final TicketReportRepository ticketReportRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final PointLogRepository pointLogRepository;

    public DetailTicketResDto detailTicket(String ticketUUID, String userUUID) {
        Ticket ticket = ticketRepository.findByTicketUUID(ticketUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.TICKET_NOT_FOUND));

        // 로그인한 사용자가 경매의 낙찰자도 주최자도 아닐 때
        if (!ticket.getWinner().getUserUUID().equals(userUUID) && !ticket.getOrganizer().getUserUUID().equals(userUUID)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        Auction auction = ticket.getAuction();
        User winner = userRepository.findByUserUUID(auction.getCurrentHighestBidderUUID())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String chatRoomUUID = ticket.getChatRoom().getChatRoomUUID();

        BigDecimal donationCommission = new BigDecimal(0.05);
        return DetailTicketResDto.builder()
                .meetingDate(auction.getMeetingDate())
                .meetingLocation(auction.getMeetingLocation())
                .organizerNickname(auction.getUser().getNickname())
                .winnerNickname(winner.getNickname())
                .winningBid(auction.getCurrentHighestBidAmount())
                .donation(BigDecimal.valueOf(auction.getCurrentHighestBidAmount()).multiply(donationCommission))
                .meetingInfoText(auction.getMeetingInfoText())
                .meetingPromiseText(auction.getMeetingPromiseText())
                .winnerIsPushed(ticket.isWinnerIsPushed())
                .organizerIsPushed(ticket.isOrganizerIsPushed())
                .chatRoomUUID(chatRoomUUID)
                .build();
    }

    @Transactional
    public void saveMeetingTime(MeetingTimeSaveReqDto reqDto, String userUUID) {
        User findUser = userRepository.findByUserUUID(userUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Ticket ticket = ticketRepository.findByTicketUUID(reqDto.getTicketUUID())
                .orElseThrow(() -> new CustomException(ErrorCode.TICKET_NOT_FOUND));

        // 로그인한 사용자가 식사권의 낙찰자도 주최자도 아닐 경우
        if (!ticket.getWinner().getUserUUID().equals(userUUID) && !ticket.getOrganizer().getUserUUID().equals(userUUID)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 로그인한 사용자가 이미 만남 시작 버튼을 눌렀을 경우
        TicketMeetingTime findTicketMeetingTime = ticketMeetingTimeRepository.findByTicketUUIDAndUserNickname(reqDto.getTicketUUID(), findUser.getNickname())
                .orElse(null);
        if (findTicketMeetingTime != null) {
            throw new CustomException(ErrorCode.TICKET_MEETING_TIME_CONFLICT);
        }

        // 로그인한 사용자에 해당하는 식사권의 만남 시작 버튼 누른 여부를 true로 변경
        if (ticket.getWinner().getUserUUID().equals(userUUID)) {
            ticket.changeWinnerIsPushed(true);
        } else if (ticket.getOrganizer().getUserUUID().equals(userUUID)) {
            ticket.changeOrganizerIsPushed(true);
        }

        TicketMeetingTime ticketMeetingTime = TicketMeetingTime.builder()
                .ticket(ticket)
                .userNickname(findUser.getNickname())
                .startMeetingTime(LocalDateTime.now())
                .build();
        ticketMeetingTimeRepository.save(ticketMeetingTime);
    }

    public LocalDateTime findMeetingTimeByTicketUUID(String ticketUUID, String userUUID) {
        Ticket ticket = ticketRepository.findByTicketUUID(ticketUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.TICKET_NOT_FOUND));

        // 로그인한 사용자가 경매의 낙찰자도 주최자도 아닐 때
        if (!ticket.getWinner().getUserUUID().equals(userUUID) && !ticket.getOrganizer().getUserUUID().equals(userUUID)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 낙찰자와 주최자 중 한명이라도 만남 시작 버튼을 누르지 않았을 경우
        if (!ticket.isWinnerIsPushed() || !ticket.isOrganizerIsPushed()) {
            throw new CustomException(ErrorCode.TICKET_MEETING_TIME_NOT_FOUND);
        }

        List<TicketMeetingTime> ticketMeetingTimes = ticket.getTicketMeetingTimes();
        if (ticketMeetingTimes.get(0).getStartMeetingTime().isAfter(ticketMeetingTimes.get(1).getStartMeetingTime())) {
            return ticketMeetingTimes.get(0).getStartMeetingTime();
        }
        return ticketMeetingTimes.get(1).getStartMeetingTime();
    }

    @Transactional
    public void reportTicket(TicketReportReqDto reqDto, String userUUID) {
        Ticket ticket = ticketRepository.findByTicketUUID(reqDto.getTicketUUID())
                .orElseThrow(() -> new CustomException(ErrorCode.TICKET_NOT_FOUND));

        // 로그인한 사용자가 경매의 낙찰자도 주최자도 아닐 때
        if (!ticket.getWinner().getUserUUID().equals(userUUID) && !ticket.getOrganizer().getUserUUID().equals(userUUID)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        String ticketReportNickname = "";
        if (ticket.getWinner().getUserUUID().equals(userUUID)) {
            ticketReportNickname = ticket.getWinner().getNickname();
        } else if (ticket.getOrganizer().getUserUUID().equals(userUUID)) {
            ticketReportNickname = ticket.getOrganizer().getNickname();
        }

        // 로그인한 사용자가 이미 해당 식사권에 대해 신고 경험이 있는 경우
        TicketReport findTicketReport = ticketReportRepository.findByTicketUUIdAndTicketReportNickname(reqDto.getTicketUUID(), ticketReportNickname)
                .orElse(null);
        if (findTicketReport != null) {
            throw new CustomException(ErrorCode.TICKET_REPORT_CONFLICT);
        }

        TicketReport ticketReport = TicketReport.builder()
                .ticket(ticket)
                .ticketReportNickname(ticketReportNickname)
                .ticketReportContent(reqDto.getTicketReportContent())
                .ticketReportEvidenceUrl(reqDto.getTicketReportEvidenceUrl())
                .build();
        ticketReportRepository.save(ticketReport);
    }

    @Transactional
    public void cancelTicket(String ticketUUID, String userUUID) {
        Ticket ticket = ticketRepository.findByTicketUUID(ticketUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.TICKET_NOT_FOUND));

        // 로그인한 사용자가 경매의 낙찰자도 주최자도 아닐 때
        if (!ticket.getWinner().getUserUUID().equals(userUUID) && !ticket.getOrganizer().getUserUUID().equals(userUUID)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        LocalDateTime meetingDateTime = ticket.getAuction().getMeetingDate();
        LocalDate meetingDate = LocalDate.of(meetingDateTime.getYear(), meetingDateTime.getMonth(),
                meetingDateTime.getDayOfMonth());

        // 현재 시간이 식사 날짜 시간을 지났을 때
        if (LocalDateTime.now().isAfter(meetingDateTime)) {
            throw new CustomException(ErrorCode.TICKET_CANCEL_CONFLICT);
        }

        // 식사권이 닫혔을 때
        if (!ticket.isTicketIsOpened()) {
            throw new CustomException(ErrorCode.ALREADY_TICKET_CANCEL_CONFLICT);
        }

        // 로그인한 사용자가 낙찰자일 때
        if (ticket.getWinner().getUserUUID().equals(userUUID)) {
            cancelTicketByWinner(ticket, meetingDate, meetingDateTime);

            // 로그인한 사용자가 주최자일 때
        } else if (ticket.getOrganizer().getUserUUID().equals(userUUID)) {
            cancelTicketByOrganizer(ticket);
        }

        // 채팅방 닫는 로직
        ticket.getChatRoom().changeChatRoomIsOpened(false);

        // 식사권 상태 false로 변경
        ticket.changeTicketIsOpened(false);
    }

    public void cancelTicketByWinner(Ticket ticket, LocalDate meetingDate, LocalDateTime meetingDateTime) {
        log.info("-----------낙찰자 식사권 취소");

        BigDecimal zero = new BigDecimal(0);
        BigDecimal vipCommission = calculateCommission(meetingDate, meetingDateTime);
        log.info("주최자 수수료 : {}", vipCommission);

        User organizer = userRepository.findByUserUUIDWithPessimisticLock(ticket.getOrganizer().getUserUUID())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        PointLog pointLog;
        // 주최자에게 수수료 제공
        if (vipCommission.compareTo(zero) == 1) {
            log.info("주최자 수수료 받기 전 포인트 : {}", organizer.getPoint());
            BigDecimal point = BigDecimal.valueOf(ticket.getAuction().getCurrentHighestBidAmount()).multiply(vipCommission);
            organizer.increasePoint(point);
            log.info("주최자 수수료 받은 후 포인트 : {}", organizer.getPoint());

            pointLog = PointLog.createPointLogWithDeposit(organizer, PointOriginType.WINNER_TICKET_CANCEL, point);
            pointLogRepository.save(pointLog);
        }

        BigDecimal basicCommission = new BigDecimal(0.7).subtract(vipCommission);
        log.info("낙찰자 수수료 : {}", basicCommission);
        // 낙찰자에게 수수료 제외한 금액 반환
        if (basicCommission.compareTo(zero) == 1) {
            User winner = userRepository.findByUserUUIDWithPessimisticLock(ticket.getWinner().getUserUUID())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            log.info("낙찰자 수수료 받기 전 포인트 : {}", winner.getPoint());
            BigDecimal point = BigDecimal.valueOf(ticket.getAuction().getCurrentHighestBidAmount()).multiply(basicCommission);
            winner.increasePoint(point);
            log.info("낙찰자 수수료 받은 후 포인트 : {}", winner.getPoint());

            pointLog = PointLog.createPointLogWithDeposit(winner, PointOriginType.WINNER_TICKET_CANCEL, point);
            pointLogRepository.save(pointLog);
        }

        // 주최자에게 최소 입찰 금액 반환
        log.info("주최자 수수료 받기 전 포인트 : {}", organizer.getPoint());
        organizer.increasePoint(BigDecimal.valueOf(ticket.getAuction().getMinBidAmount()));
        log.info("주최자 수수료 받은 후 포인트 : {}", organizer.getPoint());

        pointLog = PointLog.createPointLogWithDeposit(organizer, PointOriginType.AUCTION_REGISTRATION_DEPOSIT, BigDecimal.valueOf(ticket.getAuction().getMinBidAmount()));
        pointLogRepository.save(pointLog);
    }

    public BigDecimal calculateCommission(LocalDate meetingDate, LocalDateTime meetingDateTime) {
        BigDecimal extraCommission = new BigDecimal(0);
        // 식사 30일 ~ 22일 전
        if (LocalDate.now().isAfter(meetingDate.minusDays(31)) && LocalDate.now().isBefore(meetingDate.minusDays(21))) {
            extraCommission = new BigDecimal(0);

            // 식사 21일 ~ 15일 전
        } else if (LocalDate.now().isAfter(meetingDate.minusDays(22)) && LocalDate.now().isBefore(meetingDate.minusDays(14))) {
            extraCommission = new BigDecimal(0.1);

            //  식사 14일 ~ 8일 전
        } else if (LocalDate.now().isAfter(meetingDate.minusDays(15)) && LocalDate.now().isBefore(meetingDate.minusDays(7))) {
            extraCommission = new BigDecimal(0.3);

            // 식사 7일 ~ 1일 전
        } else if (LocalDate.now().isAfter(meetingDate.minusDays(8)) && LocalDate.now().isBefore(meetingDate)) {
            extraCommission = new BigDecimal(0.5);

            // 식사 당일 환불(식사 시간 이전까지)
        } else if (LocalDateTime.now().isBefore(meetingDateTime)) {
            extraCommission = new BigDecimal(0.7);
        }
        return extraCommission;
    }

    public void cancelTicketByOrganizer(Ticket ticket) {
        log.info("-----------주최자 식사권 취소");

        // 낙찰자에게 전체 환불 & 주최자에게는 최소 입찰 금액 반환X
        User winner = userRepository.findByUserUUIDWithPessimisticLock(ticket.getWinner().getUserUUID())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        log.info("낙찰자 환불 받기 전 포인트 : {}", winner.getPoint());
        BigDecimal point = BigDecimal.valueOf(ticket.getAuction().getCurrentHighestBidAmount());
        winner.increasePoint(point);
        log.info("낙찰자 환불 받은 후 포인트 : {}", winner.getPoint());

        PointLog pointLog = PointLog.createPointLogWithDeposit(winner, PointOriginType.ORGANIZER_TICKET_CANCEL, point);
        pointLogRepository.save(pointLog);
    }

    @Transactional
    public void saveReview(ReviewSaveReqDto reqDto, String userUUID) {
        Ticket ticket = ticketRepository.findByTicketUUID(reqDto.getTicketUUID())
                .orElseThrow(() -> new CustomException(ErrorCode.TICKET_NOT_FOUND));

        // 로그인한 사용자가 식사권의 낙찰자도 주최자도 아닐 경우
        if (!ticket.getWinner().getUserUUID().equals(userUUID) && !ticket.getOrganizer().getUserUUID().equals(userUUID)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 식사권이 닫힌 경우
        if (!ticket.isTicketIsOpened()) {
            throw new CustomException(ErrorCode.TICKET_ACCESS_DENIED);
        }

        // 해당 식사권에 대한 리뷰를 작성한 적이 있는 경우
        if (ticket.getReviews().stream().anyMatch(review -> review.getWriter().getUserUUID().equals(userUUID))) {
            throw new CustomException(ErrorCode.REVIEW_CONFLICT);
        }

        // 현재 시간이 식사 날짜 시간 + 2시간 전인 경우
        if (LocalDateTime.now().isBefore(ticket.getAuction().getMeetingDate().plusHours(2))) {
            throw new CustomException(ErrorCode.MEETING_TIME_NOT_PASSED);
        }

        User writer;
        User receiver;
        if (ticket.getWinner().getUserUUID().equals(userUUID)) {
            writer = ticket.getWinner();
            receiver = ticket.getOrganizer();
        } else {
            writer = ticket.getOrganizer();
            receiver = ticket.getWinner();
        }

        Review review = Review.builder()
                .writer(writer)
                .receiver(receiver)
                .ticket(ticket)
                .reviewContent(reqDto.getReviewContent())
                .reviewRating(reqDto.getReviewRating())
                .build();

        reviewRepository.save(review);

        List<Review> reviewsReceived = receiver.getReviewsReceived();
        double ratingAverage = Math.round(calculateUserRating(reviewsReceived) * 100) / 100.0;
        log.info("평균 별점 : {}", ratingAverage);
        receiver.updateRating(ratingAverage);
    }

    public double calculateUserRating(List<Review> reviewsReceived) {
        double sum = reviewsReceived.stream()
                .mapToDouble(Review::getReviewRating)
                .sum();
        log.info("별점 합 : {}", sum);
        return sum / reviewsReceived.size();
    }
}
