package shootingstar.var.dto.res;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAuctionSuccessResDto {
    //참여중 진행중 경매 모두 사용
    //입찰 성공 - 이름, 약속 날짜, 최종 낙찰 금액, 낙찰자
    //참여중 - 이름, 남은 시간, 현재 입찰 금액, 입찰 수
    @NotNull
    private String vipUserName;

    @NotNull
    private LocalDateTime meetDate;

    @NotNull
    private String basicUserName;

    @NotNull
    private String profileImgUrl;

    @NotNull
    private String auctionUUID;

    @NotNull
    private String ticketUUID;

    @QueryProjection
    public UserAuctionSuccessResDto(String profileImgUrl,String vipUserName, LocalDateTime meetDate, String basicUserName,String auctionUUID, String ticketUUID){
        this.profileImgUrl = profileImgUrl;
        this.vipUserName = vipUserName;
        this.meetDate = meetDate;
        this.basicUserName = basicUserName;
        this.auctionUUID = auctionUUID;
        this.ticketUUID = ticketUUID;
    }
}
