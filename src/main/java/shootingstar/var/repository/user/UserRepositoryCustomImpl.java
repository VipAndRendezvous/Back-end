package shootingstar.var.repository.user;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import shootingstar.var.dto.res.*;
import shootingstar.var.enums.type.AuctionType;
import shootingstar.var.enums.type.UserType;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static shootingstar.var.entity.QFollow.follow;
import static shootingstar.var.entity.QReview.review;
import static shootingstar.var.entity.QUser.user;
import static shootingstar.var.entity.QVipInfo.vipInfo;
import static shootingstar.var.entity.auction.QAuction.auction;

@Slf4j
public class UserRepositoryCustomImpl implements UserRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public UserRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<VipListResDto> findVipList(Pageable pageable, String search, String userUUID) {
        BooleanExpression searchCondition = search != null ? user.nickname.contains(search) : Expressions.asBoolean(true).isTrue();
        BooleanExpression vipCondition = user.userType.eq(UserType.ROLE_VIP)
                .and(user.isWithdrawn.eq(false));

        List<VipListResDto> content = queryFactory
                .select(new QVipListResDto(
                        user.userUUID,
                        user.profileImgUrl,
                        user.nickname,
                        user.rating,
                        getIsFollow(userUUID, user.userUUID)
                ))
                .from(user)
                .where(vipCondition.and(searchCondition))
                .orderBy(user.nickname.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(user.count())
                .from(user)
                .where(vipCondition.and(searchCondition));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public VipDetailResDto findVipDetailByVipUUID(String vipUUID, String userUUID) {
        VipDetailResDto vipDetailResDto = queryFactory
                .select(new QVipDetailResDto(
                        vipInfo.user.userUUID,
                        vipInfo.user.profileImgUrl,
                        vipInfo.user.nickname,
                        vipInfo.user.rating,
                        vipInfo.vipJob,
                        vipInfo.vipCareer,
                        vipInfo.vipIntroduce,
                        getIsFollow(userUUID, vipInfo.user.userUUID)
                ))
                .from(vipInfo)
                .where(vipInfo.user.userUUID.eq(vipUUID))
                .fetchOne();

        List<ProgressAuctionResDto> progressAuctionList = getProgressAutionList(vipUUID, 0L, 3);

        if (vipDetailResDto != null) {
            vipDetailResDto.setProgressAuctionList(progressAuctionList);
        }

        List<VipReceiveReviewResDto> receiveReviewList = getReceiveReviewList(vipUUID, 0L, 3);

        if (vipDetailResDto != null) {
            vipDetailResDto.setReceiveReviewList(receiveReviewList);
        }

        return vipDetailResDto;
    }

    @Override
    public Page<ProgressAuctionResDto> findVipProgressAuction(String vipUUID, Pageable pageable) {
        List<ProgressAuctionResDto> content = getProgressAutionList(vipUUID, pageable.getOffset(), pageable.getPageSize());

        JPAQuery<Long> countQuery = queryFactory.
                select(auction.count())
                .from(auction)
                .where(auction.user.userUUID.eq(vipUUID)
                        .and(auction.auctionType.eq(AuctionType.PROGRESS)));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<VipReceiveReviewResDto> findVipReceivedReview(String vipUUID, Pageable pageable) {
        List<VipReceiveReviewResDto> content = getReceiveReviewList(vipUUID, pageable.getOffset(), pageable.getPageSize());

        JPAQuery<Long> countQuery = queryFactory.
                select(review.count())
                .from(review)
                .where(review.receiver.userUUID.eq(vipUUID));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }


    private List<ProgressAuctionResDto> getProgressAutionList(String vipUUID, Long offset, Integer pageSize) {
        return queryFactory
                .select(new QProgressAuctionResDto(
                        auction.user.profileImgUrl,
                        auction.user.nickname,
                        auction.auctionUUID,
                        auction.createdTime,
                        auction.currentHighestBidAmount,
                        auction.bidCount
                ))
                .from(auction)
                .where(auction.user.userUUID.eq(vipUUID)
                        .and(auction.auctionType.eq(AuctionType.PROGRESS)))
                .orderBy(auction.createdTime.desc())
                .offset(offset)
                .limit(pageSize)
                .fetch();
    }

    private List<VipReceiveReviewResDto> getReceiveReviewList(String vipUUID, Long offset, Integer pageSize) {
        return queryFactory
                .select(new QVipReceiveReviewResDto(
                        review.writer.nickname,
                        review.reviewRating,
                        review.reviewContent
                ))
                .from(review)
                .where(review.receiver.userUUID.eq(vipUUID))
                .orderBy(review.createdTime.desc())
                .offset(offset)
                .limit(pageSize)
                .fetch();
    }

    @Override
    public Page<AllUsersDto> findAllUsers(String search, Pageable pageable) {
        List<AllUsersDto> content = queryFactory
                .select(new QAllUsersDto(
                        user.kakaoId,
                        user.userUUID,
                        user.name,
                        user.nickname,
                        user.email,
                        user.phone,
                        user.point,
                        user.userType,
                        user.warningCount
                ))
                .from(user)
                .where(user.isWithdrawn.eq(false), checkSearch(search))
                .orderBy(user.userId.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(user.count())
                .from(user)
                .where(user.isWithdrawn.eq(false), checkSearch(search));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private Expression<String> getIsFollow(String userUUID, StringPath vipUUID) {
        if (userUUID != null) {
            return queryFactory
                    .select(follow.followUUID)
                    .from(follow)
                    .where(follow.follower.userUUID.eq(userUUID)
                            .and(follow.following.userUUID.eq(vipUUID)));
        } else {
            return Expressions.constant("");
        }
    }



    private BooleanExpression checkSearch (String search) {
        if (!hasText(search)) {
            return null;
        }

        return user.name.eq(search)
                .or(user.nickname.eq(search));
    }
}
