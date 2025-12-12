package com.runfit.domain.user.controller;

import com.runfit.common.response.PageResponse;
import com.runfit.common.response.ResponseWrapper;
import com.runfit.common.response.SliceResponse;
import com.runfit.domain.auth.model.AuthUser;
import com.runfit.domain.review.controller.dto.response.ReviewResponse;
import com.runfit.domain.session.controller.dto.response.SessionListResponse;
import com.runfit.domain.user.controller.dto.request.UserUpdateRequest;
import com.runfit.domain.user.controller.dto.response.LikedSessionResponse;
import com.runfit.domain.user.controller.dto.response.UserProfileResponse;
import com.runfit.domain.user.controller.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "User", description = "사용자 API")
public interface UserApi {

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ResponseWrapper<UserResponse>> getMyInfo(
        @AuthenticationPrincipal AuthUser user
    );

    @Operation(summary = "내 정보 수정", description = "로그인한 사용자의 프로필 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ResponseWrapper<UserResponse>> updateMyInfo(
        @AuthenticationPrincipal AuthUser user,
        @Valid @RequestBody UserUpdateRequest request
    );

    @Operation(summary = "특정 유저 정보 조회", description = "특정 사용자의 공개 프로필 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    ResponseEntity<ResponseWrapper<UserProfileResponse>> getUserProfile(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "조회할 사용자 ID") @PathVariable Long userId
    );

    @Operation(summary = "내가 작성한 리뷰 목록 조회", description = "로그인한 사용자가 작성한 리뷰 목록을 조회합니다. (페이징)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ResponseWrapper<PageResponse<ReviewResponse>>> getMyReviews(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "4") int size
    );

    @Operation(summary = "내가 찜한 세션 목록 조회", description = "로그인한 사용자가 찜한 세션 목록을 조회합니다. (무한스크롤)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ResponseWrapper<SliceResponse<LikedSessionResponse>>> getMyLikedSessions(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "내가 만든 세션 목록 조회", description = "로그인한 사용자가 생성한 세션 목록을 조회합니다. (무한스크롤)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ResponseWrapper<SliceResponse<SessionListResponse>>> getMyHostedSessions(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
    );
}
