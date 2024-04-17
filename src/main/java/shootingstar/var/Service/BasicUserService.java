package shootingstar.var.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shootingstar.var.dto.req.UserApplyVipDto;
import shootingstar.var.dto.res.TicketListResDto;
import shootingstar.var.dto.res.UserAuctionInvalidityResDto;
import shootingstar.var.dto.res.UserAuctionParticipateResDto;
import shootingstar.var.dto.res.UserAuctionSuccessResDto;
import shootingstar.var.entity.auction.Auction;
import shootingstar.var.enums.type.TicketSortType;
import shootingstar.var.entity.User;
import shootingstar.var.entity.VipApprovalType;
import shootingstar.var.entity.VipInfo;
import shootingstar.var.exception.CustomException;
import shootingstar.var.repository.AuctionRepository;
import shootingstar.var.repository.user.UserRepository;
import shootingstar.var.repository.vip.VipInfoRepository;
import shootingstar.var.repository.ticket.TicketRepository;
import shootingstar.var.util.ParticipatingAuctionRedisUtil;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

import static shootingstar.var.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class BasicUserService {
    private final VipInfoRepository vipInfoRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final ParticipatingAuctionRedisUtil participatingAuctionRedisUtil;

    @Transactional
    public void applyVip(String userUUID , UserApplyVipDto userApplyVipDto){
        User user = findByUserUUID(userUUID);
        //vipinfo가 존재하는경우 예외처리
       if(user.getVipInfo()!=null){
           throw new CustomException(VIP_INFO_DUPLICATE);
       }
        VipInfo vipInfo = VipInfo.builder()
                .user(user)
                .vipName(userApplyVipDto.getVipName())
                .vipJob(userApplyVipDto.getVipJob())
                .vipCareer(userApplyVipDto.getVipCareer())
                .vipIntroduce(userApplyVipDto.getVipIntroduce())
                .vipApprovalType(VipApprovalType.STANDBY)
                .vipEvidenceUrl(userApplyVipDto.getVipEvidenceUrl())
                .build();

        vipInfoRepository.save(vipInfo);
    }

    public String applyCheck(String userUUID){
        User user = findByUserUUID(userUUID);
        VipInfo vipInfo = findVipInfoByUser(user);
        VipApprovalType vipApproval = vipInfo.getVipApproval();
        return String.valueOf(vipApproval);
    }

    public Page<TicketListResDto> getAllTicketList(String userUUID, TicketSortType ticketSortType, String search, Pageable pageable){
        return ticketRepository.findAllTicketByuserUUID(userUUID, ticketSortType, search, pageable);
    }

    public Page<UserAuctionSuccessResDto> successBeforeAuctionList(String userUUID, Pageable pageable){
        return auctionRepository.findAllSuccessBeforeByUserUUID(userUUID, pageable);
    }

    public Page<UserAuctionSuccessResDto> successAfterAuctionList(String userUUID, Pageable pageable){
        return auctionRepository.findAllSuccessAfterByUserUUID(userUUID, pageable);
    }

    public Page<UserAuctionParticipateResDto> participateAuctionList(String userUUID, Pageable pageable){
        //auctin uuid 반환
        Set<String> participateAuctionUUID = participatingAuctionRedisUtil.getParticipationList(userUUID);
        Page<UserAuctionParticipateResDto> userAuctionParticipateResDtos = auctionRepository.findAllParticipateByUserUUID(userUUID, participateAuctionUUID, pageable);
        return userAuctionParticipateResDtos;
    }

    public User findByUserUUID(String userUUID) {
        Optional<User> optionalUser = userRepository.findByUserUUID(userUUID);
        if (optionalUser.isEmpty()) {
            throw new CustomException(USER_NOT_FOUND);
        }
        return optionalUser.get();
    }

    public VipInfo findVipInfoByUser(User user) {
        Optional<VipInfo> optionalVipInfo = vipInfoRepository.findVipInfoByUser(user);
        if (optionalVipInfo.isEmpty()) {
            throw new CustomException(VIP_INFO_NOT_FOUND);
        }
        return optionalVipInfo.get();
    }
}
