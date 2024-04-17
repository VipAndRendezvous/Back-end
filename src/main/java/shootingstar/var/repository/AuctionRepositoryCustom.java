package shootingstar.var.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import shootingstar.var.dto.res.ProgressAuctionResDto;
import shootingstar.var.enums.type.AuctionSortType;
import shootingstar.var.dto.res.UserAuctionInvalidityResDto;
import shootingstar.var.dto.res.UserAuctionParticipateResDto;
import shootingstar.var.dto.res.UserAuctionSuccessResDto;

import java.util.List;
import java.util.Set;

public interface AuctionRepositoryCustom {
    Page<UserAuctionSuccessResDto> findAllSuccessAfterByUserUUID(String userUUID, Pageable pageable);
    Page<UserAuctionSuccessResDto> findAllSuccessBeforeByUserUUID(String userUUID, Pageable pageable);

    Page<UserAuctionParticipateResDto> findAllParticipateByUserUUID(String userUUID, Set<String> participateAuctionUUID, Pageable pageable);

    Page<UserAuctionSuccessResDto> findAllVipSuccessBeforeByUserUUID(String userUUID, Pageable pageable);
    Page<UserAuctionSuccessResDto> findAllVipSuccessAfterByUserUUID(String userUUID, Pageable pageable);
    Page<UserAuctionParticipateResDto> findAllVipProgressByUserUUID(String userUUID, Pageable pageable);
    Page<UserAuctionInvalidityResDto> findAllVipInvalidityByUserUUID(String userUUID,Pageable pageable);

    Page<ProgressAuctionResDto> findProgressGeneralAuction(Pageable pageable, AuctionSortType sortType, String search);
}
