package shootingstar.var.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import shootingstar.var.exception.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import shootingstar.var.Service.VipUserService;
import shootingstar.var.dto.req.VipInfoEditResDto;
import shootingstar.var.dto.res.UserAuctionInvalidityResDto;
import shootingstar.var.dto.res.UserAuctionParticipateResDto;
import shootingstar.var.dto.res.UserAuctionSuccessResDto;
import shootingstar.var.dto.res.VipInfoDto;
import shootingstar.var.enums.type.AuctionType;
import shootingstar.var.exception.CustomException;
import shootingstar.var.exception.ErrorCode;
import shootingstar.var.jwt.JwtTokenProvider;

@Tag(name = "VipUserController", description = "VIP 유저 사용 컨트롤러")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vip")

public class VipUserController {

    private final JwtTokenProvider jwtTokenProvider;
    private final VipUserService vipService;

    @Operation(summary = "vip 소개 불러오기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 아이디에 해당하는 vipInfo 반환", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = VipInfoDto.class))}),
            @ApiResponse(responseCode = "403",
                    description = "잘못된 유저 정보 : 1201\n"+"잘못된 vipInfo 정보 : 7200",
                    content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @GetMapping("/info")
    public ResponseEntity<VipInfoDto> getVipInfo(HttpServletRequest request) {
        String userUUID = jwtTokenProvider.getUserUUIDByRequest(request);
        VipInfoDto vipInfo = vipService.getVipInfo(userUUID);
        return ResponseEntity.ok(vipInfo);

    }

    @Operation(summary = "vip info 수정하기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 아이디에 해당하는 vipInfo 반환", content = {
                    @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "403",
                    description = "잘못된 유저 정보 : 1201\n"+"잘못된 vipInfo 정보 : 7200",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @PatchMapping("/editInfo")
    public ResponseEntity<String> editVipInfo(HttpServletRequest request, @RequestBody VipInfoEditResDto vipInfoEdit){
        String userUUID = jwtTokenProvider.getUserUUIDByRequest(request);
        vipService.editVipInfo(userUUID, vipInfoEdit);
        return ResponseEntity.ok("vip Info Edit Success");
    }

    @Operation(summary = "경매 불러오기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "경매 타입에 맞는 경매 불러오기", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserAuctionParticipateResDto.class))}),
            @ApiResponse(responseCode = "401",
                    description = "SUCCESS 타입의 입력 경우, : 7100",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @GetMapping("/auction/{auctionType}")
    public ResponseEntity<?> getVipAuctionList(@NotNull @PathVariable("auctionType") AuctionType auctionType,
                                               HttpServletRequest request, @PageableDefault(size = 10) Pageable pageable) {
        String userUUID = jwtTokenProvider.getUserUUIDByRequest(request);

        if (auctionType.equals(AuctionType.PROGRESS)) {
            //진행중
            Page<UserAuctionParticipateResDto> userAuctionParticipateLists = vipService.getVipUserAuctionProgress(userUUID, pageable);
            return ResponseEntity.ok(userAuctionParticipateLists);
        } else if (auctionType.equals(AuctionType.SUCCESS)) {
            throw new CustomException(ErrorCode.VIP_AUCTION_SUCCESS_ACCESS_DENIED);
        }
        else {
            //유찰
            Page<UserAuctionInvalidityResDto> userAuctionInvalidityLists = vipService.getVipUserAuctionInvalidity(userUUID,pageable);
            return ResponseEntity.ok(userAuctionInvalidityLists);
        }
    }

    @Operation(summary = "경매 성공 만남 전 불러오기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공한 경매 만남전 불러오기 성공", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserAuctionSuccessResDto.class))}),
            @ApiResponse(responseCode = "401",
                    description = "SUCCESS 타입의 입력이 아닌경우, : 7100",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @GetMapping("/auction/{auctionType}/successBefore")
    public ResponseEntity<?> getVipAuctionSuccessBefore(@NotNull @PathVariable("auctionType") AuctionType auctionType, HttpServletRequest request, @PageableDefault(size =10)Pageable pageable){
        if(!auctionType.equals(AuctionType.SUCCESS)){
            throw new CustomException(ErrorCode.VIP_AUCTION_SUCCESS_ACCESS_DENIED);
        }
        String userUUID = jwtTokenProvider.getUserUUIDByRequest(request);
        Page <UserAuctionSuccessResDto> userAuctionSuccessResDtos = vipService.getVipUserAuctionSuccessBefore(userUUID,pageable);
        return ResponseEntity.ok(userAuctionSuccessResDtos);
    }

    @Operation(summary = "경매 성공 만남 후 불러오기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공한 경매 만남후 불러오기 성공", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserAuctionSuccessResDto.class))}),
            @ApiResponse(responseCode = "401",
                    description = "SUCCESS 타입의 입력이 아닌경우, : 7100",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @GetMapping("/auction/{auctionType}/successAfter")
    public ResponseEntity<?> getVipAuctionSuccessAfter(@NotNull @PathVariable("auctionType") AuctionType auctionType,
                                                       HttpServletRequest request, @PageableDefault(size = 10) Pageable pageable){
        if(!auctionType.equals(AuctionType.SUCCESS)){
            //auctionType이 success가 아닌경우
            throw new CustomException(ErrorCode.VIP_AUCTION_SUCCESS_ACCESS_DENIED);
        }
        String userUUID = jwtTokenProvider.getUserUUIDByRequest(request);
        Page<UserAuctionSuccessResDto> userAuctionSuccessResDtos =vipService.getVipUserAuctionSuccessAfter(userUUID,pageable);
        return ResponseEntity.ok(userAuctionSuccessResDtos);
    }

}
