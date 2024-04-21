package shootingstar.var.repository.banner;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import shootingstar.var.dto.res.GetBannerResDto;
import shootingstar.var.dto.res.QGetBannerResDto;
import shootingstar.var.entity.QBanner;

import java.util.List;

import static shootingstar.var.entity.QBanner.*;

public class BannerRepositoryCustomImpl implements BannerRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    public BannerRepositoryCustomImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<GetBannerResDto> findAllBanner() {
        return queryFactory
                .select(new QGetBannerResDto(
                        banner.bannerImgUrl,
                        banner.targetUrl,
                        banner.bannerUUID
                ))
                .from(banner)
                .fetch();
    }
}

