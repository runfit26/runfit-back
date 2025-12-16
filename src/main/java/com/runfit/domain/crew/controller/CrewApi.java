package com.runfit.domain.crew.controller;

import com.runfit.common.response.PageResponse;
import com.runfit.common.response.ResponseWrapper;
import com.runfit.common.response.SliceResponse;
import com.runfit.domain.auth.model.AuthUser;
import com.runfit.domain.crew.controller.dto.request.CrewCreateRequest;
import com.runfit.domain.crew.controller.dto.request.CrewUpdateRequest;
import com.runfit.domain.crew.controller.dto.request.LeaderChangeRequest;
import com.runfit.domain.crew.controller.dto.request.RoleChangeRequest;
import com.runfit.domain.crew.controller.dto.response.CrewListResponse;
import com.runfit.domain.crew.controller.dto.response.CrewMembersResponse;
import com.runfit.domain.crew.controller.dto.response.CrewResponse;
import com.runfit.domain.crew.controller.dto.response.LeaderChangeResponse;
import com.runfit.domain.crew.controller.dto.response.MemberCountResponse;
import com.runfit.domain.crew.controller.dto.response.MemberRoleResponse;
import com.runfit.domain.crew.controller.dto.response.MembershipResponse;
import com.runfit.domain.crew.controller.dto.response.RoleChangeResponse;
import com.runfit.domain.review.controller.dto.response.CrewReviewResponse;
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

@Tag(name = "Crew", description = "크루 API")
public interface CrewApi {

    @Operation(summary = "크루 생성", description = "새로운 크루를 생성합니다. 생성자는 자동으로 LEADER가 됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "크루 생성 성공"),
        @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ResponseEntity<ResponseWrapper<CrewListResponse>> createCrew(
        @AuthenticationPrincipal AuthUser user,
        @RequestBody CrewCreateRequest request
    );

    @Operation(summary = "크루 목록 조회", description = "크루 목록을 조회합니다. 검색/필터/정렬 지원 (무한스크롤)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ResponseEntity<ResponseWrapper<SliceResponse<CrewListResponse>>> searchCrews(
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "지역 필터") @RequestParam(required = false) String city,
        @Parameter(description = "크루명 검색어") @RequestParam(required = false) String keyword,
        @Parameter(description = "정렬 (memberCountDesc: 멤버 많은 순, lastSessionDesc: 최근 세션 순, createdAtDesc: 최근 생성 순(기본값), nameAsc: 이름순 A-Z, nameDesc: 이름순 Z-A)") @RequestParam(required = false) String sort
    );

    @Operation(summary = "크루 상세 조회", description = "크루 상세 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "크루 없음")
    })
    ResponseEntity<ResponseWrapper<CrewResponse>> getCrewDetail(
        @Parameter(description = "크루 ID") @PathVariable Long crewId
    );

    @Operation(summary = "크루 정보 수정", description = "크루 정보를 수정합니다. LEADER만 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "크루 없음")
    })
    ResponseEntity<ResponseWrapper<CrewResponse>> updateCrew(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "크루 ID") @PathVariable Long crewId,
        @RequestBody CrewUpdateRequest request
    );

    @Operation(summary = "크루 삭제", description = "크루를 삭제합니다. LEADER만 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "삭제 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "크루 없음")
    })
    ResponseEntity<ResponseWrapper<String>> deleteCrew(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "크루 ID") @PathVariable Long crewId
    );

    @Operation(summary = "크루 멤버 목록 조회", description = "크루 멤버 목록을 조회합니다. role 파라미터로 필터링, sort 파라미터로 정렬 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "크루 없음")
    })
    ResponseEntity<ResponseWrapper<CrewMembersResponse>> getCrewMembers(
        @Parameter(description = "크루 ID") @PathVariable Long crewId,
        @Parameter(description = "역할 필터 (leader, staff, general)") @RequestParam(required = false) String role,
        @Parameter(description = "정렬 (joinedAtDesc: 최근 가입 순(기본값), roleAsc: 역할 순(리더→운영진→멤버))") @RequestParam(required = false) String sort
    );

    @Operation(summary = "크루 멤버 역할별 카운트 조회", description = "크루 멤버의 역할별 인원수를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "크루 없음")
    })
    ResponseEntity<ResponseWrapper<MemberCountResponse>> getMemberCount(
        @Parameter(description = "크루 ID") @PathVariable Long crewId
    );

    @Operation(summary = "특정 사용자 역할 조회", description = "크루 내 특정 사용자의 역할을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "크루 또는 멤버십 없음")
    })
    ResponseEntity<ResponseWrapper<MemberRoleResponse>> getMemberRole(
        @Parameter(description = "크루 ID") @PathVariable Long crewId,
        @Parameter(description = "사용자 ID") @PathVariable Long userId
    );

    @Operation(summary = "크루장 변경", description = "크루장을 다른 멤버에게 위임합니다. LEADER만 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "변경 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "크루 또는 멤버십 없음")
    })
    ResponseEntity<ResponseWrapper<LeaderChangeResponse>> changeLeader(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "크루 ID") @PathVariable Long crewId,
        @RequestBody LeaderChangeRequest request
    );

    @Operation(summary = "운영진 등록/해제", description = "특정 멤버를 운영진으로 등록하거나 해제합니다. LEADER만 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "변경 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "크루 또는 멤버십 없음")
    })
    ResponseEntity<ResponseWrapper<RoleChangeResponse>> changeRole(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "크루 ID") @PathVariable Long crewId,
        @Parameter(description = "대상 사용자 ID") @PathVariable Long userId,
        @RequestBody RoleChangeRequest request
    );

    @Operation(summary = "크루 멤버 강퇴", description = "크루 멤버를 강퇴합니다. LEADER만 가능합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "강퇴 성공"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "크루 또는 멤버십 없음")
    })
    ResponseEntity<ResponseWrapper<String>> kickMember(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "크루 ID") @PathVariable Long crewId,
        @Parameter(description = "대상 사용자 ID") @PathVariable Long userId
    );

    // === Membership API ===

    @Operation(summary = "크루 가입", description = "크루에 가입합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "가입 성공"),
        @ApiResponse(responseCode = "400", description = "이미 가입된 크루"),
        @ApiResponse(responseCode = "404", description = "크루 없음")
    })
    ResponseEntity<ResponseWrapper<MembershipResponse>> joinCrew(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "크루 ID") @PathVariable Long crewId
    );

    @Operation(summary = "크루 탈퇴", description = "크루에서 탈퇴합니다. 크루장은 탈퇴할 수 없습니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
        @ApiResponse(responseCode = "400", description = "크루장은 탈퇴 불가"),
        @ApiResponse(responseCode = "404", description = "크루 또는 멤버십 없음")
    })
    ResponseEntity<ResponseWrapper<String>> leaveCrew(
        @AuthenticationPrincipal AuthUser user,
        @Parameter(description = "크루 ID") @PathVariable Long crewId
    );

    @Operation(summary = "크루 리뷰 목록 조회", description = "특정 크루에서 진행된 세션들의 리뷰 목록을 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "크루 없음")
    })
    ResponseEntity<ResponseWrapper<PageResponse<CrewReviewResponse>>> getCrewReviews(
        @Parameter(description = "크루 ID") @PathVariable Long crewId,
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
    );
}
