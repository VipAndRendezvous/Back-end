package shootingstar.var.dto.res;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class GetBannerResDto {
    private String bannerImgUrl;
    private String targetUrl;
    private String bannerUUID;

    @QueryProjection
    public GetBannerResDto(String bannerImgUrl, String targetUrl, String bannerUUID) {
        this.bannerImgUrl = bannerImgUrl;
        this.targetUrl = targetUrl;
        this.bannerUUID = bannerUUID;
    }
}
