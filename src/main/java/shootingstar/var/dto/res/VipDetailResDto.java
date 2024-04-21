package shootingstar.var.dto.res;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

import java.util.List;

@Data
public class VipDetailResDto {
    private String vipUserUUID;
    private String vipProfileImgUrl;
    private String vipNickname;
    private Double vipRating;
    private String vipJop;
    private String vipCareer;
    private String vipIntroduce;
    private List<ProgressAuctionResDto> progressAuctionList;
    private List<VipReceiveReviewResDto> receiveReviewList;
    private String isFollow;

    @QueryProjection
    public VipDetailResDto(String vipUserUUID, String vipProfileImgUrl, String vipNickname, Double vipRating, String vipJop, String vipCareer, String vipIntroduce, String isFollow) {
        this.vipUserUUID = vipUserUUID;
        this.vipProfileImgUrl = vipProfileImgUrl;
        this.vipNickname = vipNickname;
        this.vipRating = vipRating;
        this.vipJop = vipJop;
        this.vipCareer = vipCareer;
        this.vipIntroduce = vipIntroduce;
        this.isFollow = isFollow;
    }
}
