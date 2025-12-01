package com.runfit.domain.review.controller;

import com.runfit.common.response.PageResponse;
import com.runfit.common.response.ResponseWrapper;
import com.runfit.domain.auth.model.AuthUser;
import com.runfit.domain.review.controller.dto.request.ReviewCreateRequest;
import com.runfit.domain.review.controller.dto.response.ReviewDeleteResponse;
import com.runfit.domain.review.controller.dto.response.ReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Review", description = "세션 리뷰 API")
public interface ReviewApi {

    @Operation(summary = "세션 리뷰 목록 조회", description = "특정 세션의 리뷰 목록을 페이지 단위로 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "세션 없음")
    })
    ResponseEntity<ResponseWrapper<PageResponse<ReviewResponse>>> getSessionReviews(
        @Parameter(description = "세션 ID") @PathVariable Long sessionId,
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "세션 리뷰 작성", description = "세션에 리뷰를 작성합니다. 해당 세션 참가자만 작성 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "리뷰 작성 성공"),
        @ApiResponse(responseCode = "400", description = "이미 리뷰 작성함 / 세션 미참가자"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "세션 없음")
    })
    ResponseEntity<ResponseWrapper<ReviewResponse>> createReview(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "세션 ID") @PathVariable Long sessionId,
        @RequestBody ReviewCreateRequest request
    );

    @Operation(summary = "리뷰 삭제", description = "리뷰를 삭제합니다. 작성자 본인만 삭제 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
        @ApiResponse(responseCode = "404", description = "리뷰 없음")
    })
    ResponseEntity<ResponseWrapper<ReviewDeleteResponse>> deleteReview(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "리뷰 ID") @PathVariable Long reviewId
    );
}
