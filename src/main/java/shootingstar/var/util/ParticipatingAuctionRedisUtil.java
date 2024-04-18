package shootingstar.var.util;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class
ParticipatingAuctionRedisUtil {
    private final RedisTemplate<String, String> participatingAuctionRedis;

    public ParticipatingAuctionRedisUtil(@Qualifier("participatingAuctionRedisTemplate") RedisTemplate<String, String> participatingAuctionRedis) {
        this.participatingAuctionRedis = participatingAuctionRedis;
    }

    // 경매 참여를 추가. 점수(score)는 만료 시간으로 사용
    // 추가적으로, 전체 Set 에 대한 최대 생명주기를 재설정
    public void addParticipation(String userId, String auctionId, long expireAt) {
        Double currentScore = participatingAuctionRedis.opsForZSet().score(userId, auctionId);

        if (currentScore == null) { // 새로운 참여 라면
            participatingAuctionRedis.opsForZSet().add(userId, auctionId, expireAt);
            // expireAt 보다 큰 첫 번째 요소를 조회
            Set<String> keys = participatingAuctionRedis.opsForZSet().rangeByScore(userId, expireAt + 1, Double.MAX_VALUE, 0, 1);
            if (keys.isEmpty()) {
                // 새로운 만료 시간을 설정
                long ttl = expireAt - System.currentTimeMillis();
                participatingAuctionRedis.expire(userId, ttl, TimeUnit.MILLISECONDS);
            }
            // 그렇지 않으면 이미 Set 에 더 늦게 만료되는 요소가 있으므로, 추가 조치가 필요 없음.
        }
    }

    // 특정 사용자의 모든 경매 참여 목록을 조회하기 전에 만료된 참여를 제거.
    public Set<String> getParticipationList(String userId) {
        removeExpiredParticipation(userId); // 만료된 참여 제거
        return participatingAuctionRedis.opsForZSet().range(userId, 0, -1);
    }

    // 만료된 경매 참여를 제거.
    public void removeExpiredParticipation(String userId) {
        long now = System.currentTimeMillis();
        participatingAuctionRedis.opsForZSet().removeRangeByScore(userId, 0, now);
    }

    // 특정 사용자의 특정 경매 참여를 제거.
    public void removeParticipation(String userId, String auctionId) {
        participatingAuctionRedis.opsForZSet().remove(userId, auctionId);
    }

    // 모든 데이터를 삭제.
    public void deleteAll() {
        Set<String> keys = participatingAuctionRedis.keys("*");
        if (keys != null && !keys.isEmpty()) {
            participatingAuctionRedis.delete(keys);
        }
    }
}
