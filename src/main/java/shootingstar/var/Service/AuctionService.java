package shootingstar.var.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shootingstar.var.entity.Auction;
import shootingstar.var.entity.User;
import shootingstar.var.exception.CustomException;
import shootingstar.var.exception.ErrorCode;
import shootingstar.var.jwt.JwtTokenProvider;
import shootingstar.var.repository.AuctionRepository;
import shootingstar.var.repository.UserRepository;
import shootingstar.var.dto.req.AuctionReqDto;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public void create(AuctionReqDto reqDto, HttpServletRequest request) {
        UUID userUUID = jwtTokenProvider.getUserUUIDByRequest(request);
        // userUUID가 null 일 때 에러 처리가 필요한지??

        User findUser = userRepository.findByUserUUID(userUUID)
                .orElse(null);
        if (findUser == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        Auction auction = Auction.builder()
                .user(findUser)
                .minBidAmount(reqDto.getMinBidAmount())
                .meetingDate(reqDto.getMeetingDate())
                .meetingLocation(reqDto.getMeetingLocation())
                .meetingInfoText(reqDto.getMeetingInfoText())
                .meetingPromiseText(reqDto.getMeetingPromiseText())
                .meetingInfoImg(reqDto.getMeetingInfoImg())
                .meetingPromiseImg(reqDto.getMeetingPromiseImg())
                .build();

        auctionRepository.save(auction);
    }
}