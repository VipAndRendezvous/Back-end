package shootingstar.var.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import shootingstar.var.exception.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import shootingstar.var.Service.UserService;
import shootingstar.var.dto.req.FollowingDto;
import shootingstar.var.dto.req.UserProfileDto;
import shootingstar.var.dto.req.WarningListDto;
import shootingstar.var.dto.res.GetBannerResDto;
import shootingstar.var.dto.res.UserReceiveReviewDto;
import shootingstar.var.dto.res.UserSendReviewDto;
import shootingstar.var.jwt.JwtTokenProvider;

import java.util.List;

@Tag(name = "UserController", description = "로그인 후 사용 가능 컨트롤러")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "프로필 불러오기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 불러오기 성공", content = {
                    @Content(mediaType = "application/json",schema = @Schema(implementation = UserProfileDto.class))}),
            @ApiResponse(responseCode = "403", description = "잘못된 유저 정보 : 1201",content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(HttpServletRequest request) {
        String userUUID = jwtTokenProvider.getUserUUIDByRequest(request);
        UserProfileDto profile = userService.getProfile(userUUID);
        return ResponseEntity.ok(profile);
    }

    @Operation(summary = "팔로우 리스트 불러오기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "팔로우 리스트 불러오기 성공", content = {
                    @Content(mediaType = "application/json",schema = @Schema(implementation = FollowingDto.class))}),
    })
    @GetMapping("/followingList")
    public ResponseEntity<?> followingList(HttpServletRequest request) {
        String userUUID = jwtTokenProvider.getUserUUIDByRequest(request);
        List<FollowingDto> followingList = userService.findAllFollowing(userUUID);
        return ResponseEntity.ok().body(followingList);
    }
    @Operation(summary = "팔로우하기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "follow success", content = {
                    @Content(mediaType = "text/plain" , schema = @Schema(implementation = String.class))
            }),
            @ApiResponse(responseCode = "403", description = "잘못된 유저 정보 : 1201",content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @GetMapping("/follow/{followingUUID}")
    public ResponseEntity<String> follow(@NotBlank @PathVariable("followingUUID") String followingUUID, HttpServletRequest request) {
        String userUUID = jwtTokenProvider.getUserUUIDByRequest(request);
        String followUUID = userService.follow(followingUUID, userUUID);
        return ResponseEntity.ok(followUUID);
    }

    @Operation(summary = "언팔로우")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "unfollow success" , content = {
                    @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
            }),
            @ApiResponse(responseCode = "403", description = "잘못된 유저 정보 : 1201",content = {
                    @Content(mediaType = "text/plain", schema = @Schema(implementation = ErrorResponse.class))}),
    })
    @DeleteMapping("/unfollow/{followUUID}")
    public ResponseEntity<String> unFollow(@NotBlank @PathVariable("followUUID") String followUUID) {
        userService.unFollow(followUUID);
        return ResponseEntity.ok().body("unfollow success");
    }

    @Operation(summary = "사용자페이지 받은 리뷰 불러오기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "받은 리뷰 불러오기 성공", content = {
                    @Content(mediaType = "application/json" , schema = @Schema(implementation = UserReceiveReviewDto.class))
            })
    })
    @GetMapping("/review/receive")
    public ResponseEntity<?> getReceiveReview(HttpServletRequest request, @PageableDefault(size=10) Pageable pageable){
        String userUUID = jwtTokenProvider.getUserUUIDByRequest(request);
        Page<UserReceiveReviewDto> userReceiveReviewDtos = userService.receiveReview(userUUID, pageable);
        return ResponseEntity.ok(userReceiveReviewDtos);
    }

    @Operation(summary = "사용자페이지 쓴 리뷰 불러오기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쓴 리뷰 불러오기 성공", content = {
                    @Content(mediaType = "application/json" , schema = @Schema(implementation = UserSendReviewDto.class))
            })
    })
    @GetMapping("/review/send")
    public ResponseEntity<?> getSendReview(HttpServletRequest request, @PageableDefault(size=10) Pageable pageable){
        String userUUID = jwtTokenProvider.getUserUUIDByRequest(request);
        Page<UserSendReviewDto> userSendReviewDtos = userService.sendReview(userUUID, pageable);
        return ResponseEntity.ok(userSendReviewDtos);
    }

    @Operation(summary = "리뷰 신고") @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "review report success",content = {
                    @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))
            })
    })
    @PostMapping("/review/report/{reviewUUID}")
    public ResponseEntity<String> reportReview(@Valid @PathVariable("reviewUUID") String reviewUUID){
        userService.reportReview(reviewUUID);
        return ResponseEntity.ok().body("review report success");
    }

    @Operation(summary = "경고 내역")
    @ApiResponse(responseCode = "200", description = "경고 내역 조회 성공",content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = WarningListDto.class))
    })
    @GetMapping("/warningList")
    public ResponseEntity<?> warningList(HttpServletRequest request){
        String userUUID = jwtTokenProvider.getUserUUIDByRequest(request);
        List<WarningListDto> warning = userService.findAllWarning(userUUID);
        return ResponseEntity.ok().body(warning);
    }


    @GetMapping("/test")
    public String test() {
        return "접근 성공";
    }

}
