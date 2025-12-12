package com.runfit.domain.session.controller;

import com.runfit.common.response.ResponseWrapper;
import com.runfit.common.response.SliceResponse;
import com.runfit.domain.auth.model.AuthUser;
import com.runfit.domain.session.controller.dto.request.SessionCreateRequest;
import com.runfit.domain.session.controller.dto.request.SessionUpdateRequest;
import com.runfit.domain.session.controller.dto.response.SessionDetailResponse;
import com.runfit.domain.session.controller.dto.response.SessionJoinResponse;
import com.runfit.domain.session.controller.dto.response.SessionLikeResponse;
import com.runfit.domain.session.controller.dto.response.SessionListResponse;
import com.runfit.domain.session.controller.dto.response.SessionParticipantsResponse;
import com.runfit.domain.session.controller.dto.response.SessionResponse;
import com.runfit.domain.session.entity.SessionLevel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Session", description = "세션(러닝 모임) API")
public interface SessionApi {

    @Operation(summary = "세션 생성", description = "새로운 세션을 생성합니다. 해당 크루의 STAFF 이상만 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "세션 생성 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (STAFF 이상만 가능)"),
        @ApiResponse(responseCode = "404", description = "크루 없음")
    })
    ResponseEntity<ResponseWrapper<SessionResponse>> createSession(
        @AuthenticationPrincipal AuthUser user,
        @RequestBody SessionCreateRequest request
    );

    @Operation(summary = "세션 목록 조회", description = "세션 목록을 조회합니다. 검색/필터/정렬 지원 (무한스크롤)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<ResponseWrapper<SliceResponse<SessionListResponse>>> searchSessions(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "도시 필터 (복수 선택 가능, 예: 서울, 경기, 충북)") @RequestParam(required = false) List<String> city,
        @Parameter(description = "시/군/구 필터 (복수 선택 가능, 예: 강남구, 가평군)") @RequestParam(required = false) List<String> district,
        @Parameter(description = "크루 ID 필터") @RequestParam(required = false) Long crewId,
        @Parameter(description = "난이도 필터 (BEGINNER, INTERMEDIATE, ADVANCED)") @RequestParam(required = false) SessionLevel level,
        @Parameter(description = "시작 날짜 필터 (yyyy-MM-dd, sessionAt 기준)") @RequestParam(required = false) LocalDate dateFrom,
        @Parameter(description = "종료 날짜 필터 (yyyy-MM-dd, sessionAt 기준)") @RequestParam(required = false) LocalDate dateTo,
        @Parameter(description = "시작 시간 필터 (HH:mm, sessionAt 기준)") @RequestParam(required = false) LocalTime timeFrom,
        @Parameter(description = "종료 시간 필터 (HH:mm, sessionAt 기준)") @RequestParam(required = false) LocalTime timeTo,
        @Parameter(description = "정렬 (createdAtDesc: 최근생성순(기본), sessionAtAsc: 모임시작일순, registerByAsc: 마감임박순)") @RequestParam(required = false) String sort
    );

    @Operation(summary = "세션 상세 조회", description = "세션 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "세션 없음")
    })
    ResponseEntity<ResponseWrapper<SessionDetailResponse>> getSessionDetail(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "세션 ID") @PathVariable Long sessionId
    );

    @Operation(summary = "세션 참가 신청", description = "세션에 참가를 신청합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "참가 신청 성공"),
        @ApiResponse(responseCode = "400", description = "정원 초과 / 이미 참가 / 마감됨"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "세션 없음")
    })
    ResponseEntity<ResponseWrapper<SessionJoinResponse>> joinSession(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "세션 ID") @PathVariable Long sessionId
    );

    @Operation(summary = "세션 참가 취소", description = "세션 참가를 취소합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "참가 취소 성공"),
        @ApiResponse(responseCode = "400", description = "참가하지 않은 세션"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "세션 없음")
    })
    ResponseEntity<ResponseWrapper<SessionJoinResponse>> cancelJoinSession(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "세션 ID") @PathVariable Long sessionId
    );

    @Operation(summary = "세션 찜", description = "세션을 찜 목록에 추가합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "찜 성공"),
        @ApiResponse(responseCode = "400", description = "이미 찜한 세션"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "세션 없음")
    })
    ResponseEntity<ResponseWrapper<SessionLikeResponse>> likeSession(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "세션 ID") @PathVariable Long sessionId
    );

    @Operation(summary = "세션 찜 취소", description = "세션 찜을 취소합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "찜 취소 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "404", description = "세션 없음 / 찜하지 않은 세션")
    })
    ResponseEntity<ResponseWrapper<SessionLikeResponse>> unlikeSession(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "세션 ID") @PathVariable Long sessionId
    );

    @Operation(summary = "세션 참가자 목록 조회", description = "해당 세션에 참가 신청한 사용자 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "세션 없음")
    })
    ResponseEntity<ResponseWrapper<SessionParticipantsResponse>> getSessionParticipants(
        @Parameter(description = "세션 ID") @PathVariable Long sessionId
    );

    @Operation(summary = "세션 정보 수정", description = "세션 정보를 수정합니다. 해당 크루의 STAFF 이상만 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (STAFF 이상만 가능)"),
        @ApiResponse(responseCode = "404", description = "세션 없음")
    })
    ResponseEntity<ResponseWrapper<SessionResponse>> updateSession(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "세션 ID") @PathVariable Long sessionId,
        @RequestBody SessionUpdateRequest request
    );

    @Operation(summary = "세션 삭제", description = "세션을 삭제합니다. 세션 생성자만 삭제 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (세션 생성자만 가능)"),
        @ApiResponse(responseCode = "404", description = "세션 없음")
    })
    ResponseEntity<ResponseWrapper<Map<String, String>>> deleteSession(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "세션 ID") @PathVariable Long sessionId
    );
}
