package shootingstar.var.jwt;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class TokenProperty {
//    private final int ACCESS_EXPIRE = 3 * 60 * 60 * 1000 ; // 3시간 밀리 세컨드
//    private final long ACCESS_EXPIRE = 30L * 24 * 60 * 60 * 1000 ; // 30일 밀리 세컨드
    private final long ACCESS_EXPIRE = 10 * 60 * 1000 ; // 1분
//    private final int REFRESH_EXPIRE = 24 * 60 * 60 * 1000 ; // 24시간 밀리 세컨드
    private final int REFRESH_EXPIRE = 24 * 60 * 60 * 1000 ; // 24시간 밀리 세컨드
}
