package shootingstar.var.Service;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import shootingstar.var.dto.req.FollowingDto;
import shootingstar.var.dto.req.UserProfileDto;
import shootingstar.var.dto.res.UserAuctionParticipateResDto;
import shootingstar.var.dto.res.UserAuctionSuccessResDto;
import shootingstar.var.entity.Follow;
import shootingstar.var.entity.User;
import shootingstar.var.entity.VipApprovalType;
import shootingstar.var.entity.VipInfo;
import shootingstar.var.entity.auction.Auction;
import shootingstar.var.entity.ticket.Ticket;
import shootingstar.var.enums.type.AuctionType;
import shootingstar.var.enums.type.UserType;
import shootingstar.var.repository.AuctionRepository;
import shootingstar.var.repository.follow.FollowRepository;
import shootingstar.var.repository.ticket.TicketRepository;
import shootingstar.var.repository.user.UserRepository;
import shootingstar.var.repository.vip.VipInfoRepository;
import shootingstar.var.util.ParticipatingAuctionRedisUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VipInfoRepository vipInfoRepository;
    @Autowired
    private BasicUserService basicUserService;
    @Autowired
    private AdminService adminService;
    @Autowired
    private FollowRepository followRepository;
    @Autowired
    private AuctionRepository auctionRepository;
    @Autowired
    private ParticipatingAuctionRedisUtil participatingAuctionRedisUtil;
    @Autowired
    private TicketRepository ticketRepository;
    @Test
    @DisplayName("팔로우리스트 가져오기")
    @Transactional
    public void getFollowList()  throws  Exception{
        User vip1 = new User("11", "실명1", "유명인1", "000-0000-0000", "test@ttt.com", "helloUrl", UserType.ROLE_VIP);
        User vip2 = new User("22", "실명2", "유명인2", "000-0000-0000", "test@ttt.com", "helloUrl", UserType.ROLE_VIP);
        User vip3 = new User("33", "실명3", "유명인3", "000-0000-0000", "test@ttt.com", "helloUrl", UserType.ROLE_VIP);
        User basic = new User("44", "실명", "일반인", "000-0000-0000", "test@ttt.com", "helloUrl", UserType.ROLE_BASIC);

        userRepository.save(vip1);
        userRepository.save(vip2);
        userRepository.save(vip3);
        userRepository.save(basic);

        userRepository.flush();

        userService.follow(vip1.getUserUUID(), basic.getUserUUID());
        userService.follow(vip2.getUserUUID(), basic.getUserUUID());
        userService.follow(vip3.getUserUUID(), basic.getUserUUID());
        List<FollowingDto> followingDto =userService.findAllFollowing(basic.getUserUUID());
        System.out.println(followingDto);



    }

    @Test
    @DisplayName("unfollow")
    @Transactional
    public void unfollow() throws Exception{
        User vip1 = new User("11", "실명1", "유명인1", "000-0000-0000", "test@ttt.com", "helloUrl", UserType.ROLE_VIP);
        User vip2 = new User("22", "실명2", "유명인2", "000-0000-0000", "test@ttt.com", "helloUrl", UserType.ROLE_VIP);
        User vip3 = new User("33", "실명3", "유명인3", "000-0000-0000", "test@ttt.com", "helloUrl", UserType.ROLE_VIP);
        User basic = new User("44", "실명", "일반인", "000-0000-0000", "test@ttt.com", "helloUrl", UserType.ROLE_BASIC);

        userRepository.save(vip1);
        userRepository.save(vip2);
        userRepository.save(vip3);
        userRepository.save(basic);

        userRepository.flush();

        //userService.unFollow();
        Follow follow1 = new Follow(basic, vip1);
        Follow follow2 = new Follow(basic, vip2);
        Follow follow3 = new Follow(basic, vip3);

        followRepository.save(follow1);
        followRepository.save(follow2);
        followRepository.save(follow3);

        followRepository.flush();
        List<FollowingDto> followingDto =userService.findAllFollowing(basic.getUserUUID());
        System.out.println(followingDto);
        System.out.println(followingDto.size());


        userService.unFollow(follow1.getFollowUUID());
        List<FollowingDto> followingDto1 =userService.findAllFollowing(basic.getUserUUID());
        System.out.println(followingDto1);
        System.out.println(followingDto1.size());
    }
    @Test
    @DisplayName("프로필 불러오기")
    @Transactional
    public void getProfile() throws Exception {
        User vip = new User("22", "실명", "유명인", "000-0000-0000", "test@ttt.com", "helloUrl", UserType.ROLE_VIP);
        User basic = new User("33", "실명", "일반인", "000-0000-0000", "test@ttt.com", "helloUrl", UserType.ROLE_BASIC);

        userRepository.save(vip);
        userRepository.save(basic);

        userRepository.flush();

        UserProfileDto userProfileDto = userService.getProfile(vip.getUserUUID());
        System.out.println(userProfileDto.getNickname()+" "+ userProfileDto.getPoint()+" "+userProfileDto.getRating());
        UserProfileDto userProfileDto1 = userService.getProfile(basic.getUserUUID());
        System.out.println(userProfileDto1.getNickname()+" "+ userProfileDto1.getPoint()+" "+userProfileDto1.getRating());
    }

//    @Test
//    @DisplayName("vip 신청 상태 조회")
//    @Transactional
//    public void applyCheck() throws Exception {
//        User basic = new User("33", "실명", "일반인", "000-0000-0000", "test@ttt.com", "helloUrl", UserType.ROLE_BASIC);
//        userRepository.save(basic);
//        userRepository.flush();
//
//        VipInfo vipInfo = new VipInfo(basic,"일반인","직업","경력","소개", VipApprovalType.STANDBY,"url");
//        vipInfoRepository.save(vipInfo);
//        vipInfoRepository.flush();
//
//        basicUserService.applyCheck(basic.getUserUUID());
//        System.out.println(basicUserService.applyCheck(basic.getUserUUID()));
//
//        adminService.vipInfoChange(vipInfo.getVipInfoUUID(), "APPROVE");
//        System.out.println(basicUserService.applyCheck(basic.getUserUUID()));
//    }

    @Test
    @DisplayName("성공한 경매 조회")
    @Transactional
    public void getSuccessAuctionList() throws  Exception{
        User vip = new User("22", "실명", "유명인", "000-0000-0000", "test@ttt.com", "helloUrl", UserType.ROLE_VIP);
        User basic = new User("33", "실명", "일반인", "000-0000-0000", "test@ttt.com", "helloUrl", UserType.ROLE_BASIC);

        userRepository.save(vip);
        userRepository.save(basic);

        userRepository.flush();

        Auction successAuction = new Auction(vip, 100000L, LocalDateTime.of(2025,01,01, 01,01), "위치", "정보", "약속", "img", "img", LocalDateTime.now().plusDays(3));
        Auction successAuction1 = new Auction(vip, 100000L, LocalDateTime.of(2025,01,01, 01,01), "위치", "정보", "약속", "img", "img", LocalDateTime.now().plusDays(3));
        Auction successAuction2 = new Auction(vip, 100000L, LocalDateTime.of(2023,01,01, 01,01), "위치", "정보", "약속", "img", "img", LocalDateTime.now().plusDays(3));
        Auction successAuction3 = new Auction(vip, 100000L, LocalDateTime.of(2023,01,01, 01,01), "위치", "정보", "약속", "img", "img", LocalDateTime.now().plusDays(3));

        successAuction.changeCurrentHighestBidderUUID(basic.getUserUUID());
        successAuction1.changeCurrentHighestBidderUUID(basic.getUserUUID());
        successAuction2.changeCurrentHighestBidderUUID(basic.getUserUUID());
        successAuction3.changeCurrentHighestBidderUUID(basic.getUserUUID());

        successAuction.changeAuctionType(AuctionType.SUCCESS);
        successAuction1.changeAuctionType(AuctionType.SUCCESS);
        successAuction2.changeAuctionType(AuctionType.SUCCESS);
        successAuction3.changeAuctionType(AuctionType.SUCCESS);

        Ticket ticket = new Ticket(successAuction, basic, vip);
        Ticket ticket1 = new Ticket(successAuction1, basic, vip);
        Ticket ticket2 = new Ticket(successAuction2, basic, vip);
        Ticket ticket3 = new Ticket(successAuction3, basic, vip);

        successAuction.changeCurrentHighestBidAmount(100000);
        successAuction1.changeCurrentHighestBidAmount(100000);
        successAuction2.changeCurrentHighestBidAmount(100000);
        successAuction3.changeCurrentHighestBidAmount(100000);

        auctionRepository.save(successAuction);
        auctionRepository.save(successAuction1);
        auctionRepository.save(successAuction2);
        auctionRepository.save(successAuction3);

        auctionRepository.flush();



        ticketRepository.save(ticket);
        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);

        ticketRepository.flush();


        Page<UserAuctionSuccessResDto> userAuctionSuccessResDtos = basicUserService.successBeforeAuctionList(basic.getUserUUID(), Pageable.unpaged());
        Page<UserAuctionSuccessResDto> userAuctionSuccessResDtos1 = basicUserService.successAfterAuctionList(basic.getUserUUID(), Pageable.unpaged());
        System.out.println(userAuctionSuccessResDtos.stream().toList());
        System.out.println(userAuctionSuccessResDtos1.stream().toList());
//낙찰된경매만 불러오는거
    }

    @Test
    @DisplayName("참여중인 경매 조회")
    @Transactional
    public void getParticipateList() throws Exception{
        User vip = new User("22", "실명", "유명인", "000-0000-0000", "test@ttt.com", "helloUrl", UserType.ROLE_VIP);
        User basic = new User("33", "실명", "일반인", "000-0000-0000", "test@ttt.com", "helloUrl", UserType.ROLE_BASIC);

        userRepository.save(vip);
        userRepository.save(basic);

        userRepository.flush();

        Auction progressAuction = new Auction(vip, 100000L, LocalDateTime.now(), "위치", "정보", "약속", "img", "img", LocalDateTime.now().plusDays(3));
        Auction progressAuction1 = new Auction(vip, 100000L, LocalDateTime.now(), "위치", "정보", "약속", "img", "img", LocalDateTime.now().plusDays(3));
        Auction progressAuction2 = new Auction(vip, 100000L, LocalDateTime.now(), "위치", "정보", "약속", "img", "img", LocalDateTime.now().plusDays(3));
        Auction progressAuction3 = new Auction(vip, 100000L, LocalDateTime.now(), "위치", "정보", "약속", "img", "img", LocalDateTime.now().plusDays(3));
        Auction progressAuction4 = new Auction(vip, 100000L, LocalDateTime.now(), "위치", "정보", "약속", "img", "img", LocalDateTime.now().plusDays(3));

        auctionRepository.save(progressAuction);
        auctionRepository.save(progressAuction1);
        auctionRepository.save(progressAuction2);
        auctionRepository.save(progressAuction3);
        auctionRepository.save(progressAuction4);

        auctionRepository.flush();

        LocalDateTime closeTime = progressAuction.getAuctionCloseTime();
        long expiredMilliSeconds = closeTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        System.out.println(progressAuction.getAuctionUUID());
        participatingAuctionRedisUtil.addParticipation(basic.getUserUUID(), progressAuction.getAuctionUUID(), expiredMilliSeconds);

        LocalDateTime closeTime1 = progressAuction1.getAuctionCloseTime();
        long expiredMilliSeconds1 = closeTime1.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        System.out.println(progressAuction1.getAuctionUUID());
        participatingAuctionRedisUtil.addParticipation(basic.getUserUUID(), progressAuction1.getAuctionUUID(), expiredMilliSeconds1);

        Set<String> participationList = participatingAuctionRedisUtil.getParticipationList(basic.getUserUUID());
        System.out.println(participationList);
        Page<UserAuctionParticipateResDto> userAuctionParticipateResDtos = basicUserService.participateAuctionList(basic.getUserUUID(), Pageable.unpaged());
        System.out.print(userAuctionParticipateResDtos.stream().toList());

        participatingAuctionRedisUtil.removeParticipation(basic.getUserUUID(), progressAuction.getAuctionUUID());
        participatingAuctionRedisUtil.removeParticipation(basic.getUserUUID(), progressAuction1.getAuctionUUID());



    }
}
