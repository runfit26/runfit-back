package com.runfit.domain.user.controller;

import com.runfit.common.response.ResponseWrapper;
import com.runfit.common.response.SliceResponse;
import com.runfit.domain.auth.model.AuthUser;
import com.runfit.domain.user.controller.dto.response.LikedSessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "User", description = "사용자 API")
public interface UserApi {

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
}
