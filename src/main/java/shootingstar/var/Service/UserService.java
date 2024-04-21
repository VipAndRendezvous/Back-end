package shootingstar.var.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import shootingstar.var.dto.req.FollowingDto;
import shootingstar.var.dto.req.UserProfileDto;
import org.springframework.transaction.annotation.Transactional;
import shootingstar.var.dto.req.WarningListDto;
import shootingstar.var.dto.res.UserReceiveReviewDto;
import shootingstar.var.dto.res.UserSendReviewDto;
import shootingstar.var.entity.*;
import shootingstar.var.entity.auction.Auction;
import shootingstar.var.enums.type.UserType;
import shootingstar.var.exception.CustomException;
import shootingstar.var.repository.follow.FollowRepository;
import shootingstar.var.repository.review.ReviewRepository;
import shootingstar.var.repository.reviewReport.ReviewReportRepository;
import shootingstar.var.repository.user.UserRepository;
import shootingstar.var.repository.warning.WarningRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static shootingstar.var.exception.ErrorCode.REVIEW_NOT_FOUND;
import static shootingstar.var.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final WarningRepository warningRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewReportRepository reviewReportRepository;

    public boolean checkVIP(String userUUID) {
        User user = findByUserUUID(userUUID);
        if (user.getUserType().equals(UserType.ROLE_VIP)) {
            //vip인 경우 true
            return true;
        } else {
            //vip가 아닌경우 false
            return false;
        }
    }

    public UserProfileDto getProfile(String userUUID) {
        User user = findByUserUUID(userUUID);
        UserProfileDto userProfileDto = new UserProfileDto(user.getNickname(),user.getUserUUID(), user.getProfileImgUrl(),
                user.getDonationPrice(), user.getPoint(), user.getSubscribeExpiration(), user.getRating() , user.getUserType());
        return userProfileDto;
    }

    public List<FollowingDto> findAllFollowing(String userUUID) {
        return followRepository.findAllByFollowerId(userUUID);
    }

    @Transactional
    public String follow(String followingUUID, String userUUID) {
        User follower = findByUserUUID(userUUID);
        User following = findByUserUUID(followingUUID);
        Follow follow = new Follow(follower,following);
        followRepository.save(follow);
        return follow.getFollowUUID();
    }

    @Transactional
    public void unFollow(String followUUID) {
        Follow follow = findFollowingByFollowUUID(followUUID);
        followRepository.delete(follow);
    }

    public Page<UserReceiveReviewDto> receiveReview(String userUUID, Pageable pageable){
        return reviewRepository.findAllReceiveByUserUUID(userUUID,pageable);
    }

    public Page<UserSendReviewDto> sendReview(String userUUID, Pageable pageable){
        return reviewRepository.findAllSendByUserUUID(userUUID,pageable);
    }
    public List<WarningListDto> findAllWarning(String userUUID) {
        return warningRepository.findAllWarnByUserUUID(userUUID);
    }

    @Transactional
    public void reportReview(Long reviewId){
        Review review = findByReviewId(reviewId);
        ReviewReport reviewReport = new ReviewReport(
                review,
                review.getReviewContent(),
                ReviewReportStatus.STANDBY
        );
        reviewReportRepository.save(reviewReport);
    }

    private Follow findFollowingByFollowUUID(String followUUID) {
        Optional<Follow> followOptional = followRepository.findByFollowUUID(followUUID);
        if (followOptional.isEmpty()) {
            throw new CustomException(USER_NOT_FOUND);
        }
        return followOptional.get();
    }


    public User findByUserUUID(String userUUID) {
        Optional<User> optionalUser = userRepository.findByUserUUID(userUUID);
        if (optionalUser.isEmpty()) {
            throw new CustomException(USER_NOT_FOUND);
        }
        return optionalUser.get();
    }

    public Review findByReviewId(Long reviewId){
        Optional<Review> optionalReview = reviewRepository.findById(reviewId);
        if(optionalReview.isEmpty()){
            throw new CustomException(REVIEW_NOT_FOUND);
        }
        return optionalReview.get();
    }

}