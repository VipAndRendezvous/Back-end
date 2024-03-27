package shootingstar.var.Service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shootingstar.var.dto.req.MeetingTimeSaveReqDto;
import shootingstar.var.dto.res.DetailTicketResDto;
import shootingstar.var.entity.Auction;
import shootingstar.var.entity.Ticket;
import shootingstar.var.entity.TicketMeetingTime;
import shootingstar.var.entity.User;
import shootingstar.var.exception.CustomException;
import shootingstar.var.exception.ErrorCode;
import shootingstar.var.repository.TicketMeetingTimeRepository;
import shootingstar.var.repository.TicketRepository;
import shootingstar.var.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketMeetingTimeRepository ticketMeetingTimeRepository;
    private final UserRepository userRepository;

    public DetailTicketResDto detailTicket(String ticketUUID, String userUUID) {
        Ticket ticket = ticketRepository.findByTicketUUID(ticketUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.TICKET_NOT_FOUND));

        // 로그인한 사용자가 경매의 낙찰자도 주최자도 아닐 때
        if (!ticket.getWinner().getUserUUID().equals(userUUID) && !ticket.getOrganizer().getUserUUID().equals(userUUID)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        Auction auction = ticket.getAuction();
        User winner = userRepository.findByUserUUID(auction.getCurrentHighestBidderId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return DetailTicketResDto.builder()
                .meetingDate(auction.getMeetingDate())
                .meetingLocation(auction.getMeetingLocation())
                .organizerNickname(auction.getUser().getNickname())
                .winnerNickname(winner.getNickname())
                .winningBid(auction.getCurrentHighestBidAmount())
                .donation(auction.getCurrentHighestBidAmount() * 0.05)
                .meetingInfoText(auction.getMeetingInfoText())
                .meetingPromiseText(auction.getMeetingPromiseText())
                .winnerIsPushed(ticket.isWinnerIsPushed())
                .organizerIsPushed(ticket.isOrganizerIsPushed())
                .build();
    }

    @Transactional
    public void saveMeetingTime(MeetingTimeSaveReqDto reqDto, String userUUID) {
        User findUser = userRepository.findByUserUUID(userUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Ticket ticket = ticketRepository.findById(reqDto.getTicketId())
                .orElseThrow(() -> new CustomException(ErrorCode.TICKET_NOT_FOUND));

        // 로그인한 사용자가 식사권의 낙찰자도 주최자도 아닐 경우
        if (!ticket.getWinner().getUserUUID().equals(userUUID) && !ticket.getOrganizer().getUserUUID().equals(userUUID)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 로그인한 사용자가 이미 만남 시작 버튼을 눌렀을 경우
        TicketMeetingTime findTicketMeetingTime = ticketMeetingTimeRepository.findByTicketIdAndUserNickname(reqDto.getTicketId(), findUser.getNickname())
                .orElse(null);
        if (findTicketMeetingTime != null) {
            throw new CustomException(ErrorCode.TICKET_CONFLICT);
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
                .startMeetingTime(LocalDateTime.parse(reqDto.getStartMeetingTime()))
                .build();
        ticketMeetingTimeRepository.save(ticketMeetingTime);
    }
}