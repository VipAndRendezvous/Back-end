package shootingstar.var.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shootingstar.var.dto.req.UserSignupReqDto;
import shootingstar.var.dto.res.*;
import shootingstar.var.entity.auction.Auction;
import shootingstar.var.entity.User;
import shootingstar.var.entity.Wallet;
import shootingstar.var.enums.type.AuctionSortType;
import shootingstar.var.enums.type.UserType;
import shootingstar.var.exception.CustomException;
import shootingstar.var.exception.ErrorCode;
import shootingstar.var.jwt.JwtTokenProvider;
import shootingstar.var.repository.AuctionRepository;
import shootingstar.var.repository.user.UserRepository;
import shootingstar.var.repository.banner.BannerRepository;
import shootingstar.var.repository.wallet.WalletRepository;
import shootingstar.var.util.MailRedisUtil;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AllUserService {
    private final UserRepository userRepository;
    private final BannerRepository bannerRepository;
    private final AuctionRepository auctionRepository;
    private final WalletRepository walletRepository;

    private final MailRedisUtil mailRedisUtil;
    private final CheckDuplicateService duplicateService;
    private final JwtTokenProvider tokenProvider;

    public void signup(UserSignupReqDto reqDto) {
        if (duplicateService.checkEmailDuplicate(reqDto.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (duplicateService.checkNicknameDuplicate(reqDto.getNickname())) {
            throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
        }

        if (mailRedisUtil.hasKey(reqDto.getEmail()) && mailRedisUtil.getData(reqDto.getEmail()).equals("validate")) { // 이메일 인증을 받은 이메일 인지 확인

            User user = User.builder()
                    .kakaoId(reqDto.getKakaoId())
                    .name(reqDto.getUserName())
                    .nickname(reqDto.getNickname())
                    .phone("000-0000-0000")
                    .email(reqDto.getEmail())
                    .profileImgUrl(reqDto.getProfileImgUrl())
                    .userType(UserType.ROLE_BASIC)
                    .build();

            userRepository.save(user);
            mailRedisUtil.deleteData(reqDto.getEmail());
        } else {
            throw new CustomException(ErrorCode.VALIDATE_ERROR_EMAIL);
        }
    }

    public List<GetBannerResDto> getBanner() {
        return bannerRepository.findAllBanner();
    }

    @Transactional
    public BigDecimal getTotalDonation() {
        Wallet wallet = walletRepository.findWithPessimisticLock()
                .orElseThrow(() -> new CustomException(ErrorCode.WALLET_NOT_FOUND));
        return wallet.getTotalDonationPrice();
    }

    public Page<VipListResDto> getVipList(Pageable pageable, String search, String accessToken) {
        String userUUID = null;

        if (accessToken != null) {
            Authentication authentication = tokenProvider.getAuthenticationFromAccessToken(accessToken);
            String tokenUserUUID = authentication.getName();
            User user = userRepository.findByUserUUID(tokenUserUUID)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            userUUID = tokenUserUUID;
        }
        return userRepository.findVipList(pageable, search, userUUID);
    }

    public VipDetailResDto getVipDetail(String vipUUID, String accessToken) {
        User vip = checkUserAndVipRole(vipUUID);

        String userUUID = null;

        if (accessToken != null) {
            Authentication authentication = tokenProvider.getAuthenticationFromAccessToken(accessToken);
            String tokenUserUUID = authentication.getName();
            User user = userRepository.findByUserUUID(tokenUserUUID)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            userUUID = tokenUserUUID;
        }
        return userRepository.findVipDetailByVipUUID(vipUUID, userUUID);
    }

    public Page<ProgressAuctionResDto> getVipProgressAuction(String vipUUID, Pageable pageable) {
        User vip = checkUserAndVipRole(vipUUID);

        return userRepository.findVipProgressAuction(vipUUID, pageable);
    }

    public Page<VipReceiveReviewResDto> getVipReceivedReview(String vipUUID, Pageable pageable) {
        User vip = checkUserAndVipRole(vipUUID);
        return userRepository.findVipReceivedReview(vipUUID, pageable);
    }

    public Page<ProgressAuctionResDto> getProgressGeneralAuction(Pageable pageable, AuctionSortType sortType, String search) {
        return auctionRepository.findProgressGeneralAuction(pageable, sortType, search);
    }

    public AuctionDetailResDto getAuctionDetail(String auctionUUID) {
        Auction auction = auctionRepository.findByAuctionUUID(auctionUUID).orElseThrow(
                () -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));

        User vip = auction.getUser();

        String location = auction.getMeetingLocation();
        String[] parts = location.split(" ");
        String trimmedLocation;
        try {
            trimmedLocation = parts[0] + " " + parts[1];
        } catch (Exception e) {
            trimmedLocation = location;
        }

        return AuctionDetailResDto.builder()
                .vipUUID(vip.getUserUUID())
                .vipNickname(vip.getNickname())
                .vipProfileImgUrl(vip.getProfileImgUrl())
                .vipRating(vip.getRating())
                .auctionUUID(auction.getAuctionUUID())
                .auctionCreatedTime(auction.getCreatedTime())
                .meetingDate(auction.getMeetingDate())
                .meetingLocation(trimmedLocation)
                .currentHighestBidAmount(auction.getCurrentHighestBidAmount())
                .meetingInfoText(auction.getMeetingInfoText())
                .meetingPromiseText(auction.getMeetingPromiseText())
                .build();
    }

    private User checkUserAndVipRole(String vipUUID) {
        User vip = userRepository.findByUserUUID(vipUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!vip.getUserType().equals(UserType.ROLE_VIP)) {
            throw new CustomException(ErrorCode.VIP_INFO_NOT_FOUND);
        }

        return vip;
    }
}
