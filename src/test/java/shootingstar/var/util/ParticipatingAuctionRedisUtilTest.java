package shootingstar.var.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import shootingstar.var.entity.auction.Auction;
import shootingstar.var.entity.User;
import shootingstar.var.enums.type.UserType;
import shootingstar.var.repository.AuctionRepository;
import shootingstar.var.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

@SpringBootTest
class ParticipatingAuctionRedisUtilTest {

    @Autowired
    private ParticipatingAuctionRedisUtil participatingAuctionRedisUtil;

    @Autowired
    private AuctionRepository auctionRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    public void redisTest() throws Exception {
        //given
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

        //when
        LocalDateTime closeTime = progressAuction.getAuctionCloseTime();
        long expiredMilliSeconds = closeTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        System.out.println(progressAuction.getAuctionUUID());
        participatingAuctionRedisUtil.addParticipation(basic.getUserUUID(), progressAuction.getAuctionUUID(), expiredMilliSeconds);

        LocalDateTime closeTime1 = progressAuction1.getAuctionCloseTime();
        long expiredMilliSeconds1 = closeTime1.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        System.out.println(progressAuction1.getAuctionUUID());
        participatingAuctionRedisUtil.addParticipation(basic.getUserUUID(), progressAuction1.getAuctionUUID(), expiredMilliSeconds1);

        //then
        Set<String> participationList = participatingAuctionRedisUtil.getParticipationList(basic.getUserUUID());
        //자기 유저유유아이디 -> 참여하는 경매 유유아이디
        System.out.print(participationList);
        Assertions.assertThat(participationList).size().isEqualTo(2);

        participatingAuctionRedisUtil.deleteAll();
    }
}