package com.runfit.domain.crew.controller;

import com.runfit.common.response.ResponseWrapper;
import com.runfit.common.response.SliceResponse;
import com.runfit.domain.auth.model.AuthUser;
import com.runfit.domain.crew.controller.dto.request.CrewCreateRequest;
import com.runfit.domain.crew.controller.dto.request.CrewSearchCondition;
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
import com.runfit.domain.crew.service.CrewService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crews")
@RequiredArgsConstructor
public class CrewController implements CrewApi {

    private final CrewService crewService;

    @Override
    @PostMapping
    public ResponseEntity<ResponseWrapper<CrewListResponse>> createCrew(
        @AuthenticationPrincipal AuthUser user,
        @Valid @RequestBody CrewCreateRequest request
    ) {
        CrewListResponse response = crewService.createCrew(user.userId(), request);
        URI location = URI.create("/api/crews/" + response.id());
        return ResponseEntity.created(location).body(ResponseWrapper.success(response));
    }

    @Override
    @GetMapping
    public ResponseEntity<ResponseWrapper<SliceResponse<CrewListResponse>>> searchCrews(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String region,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String sort
    ) {
        CrewSearchCondition condition = CrewSearchCondition.of(region, keyword, sort);
        Slice<CrewListResponse> result = crewService.searchCrews(condition, PageRequest.of(page, size));
        return ResponseEntity.ok(ResponseWrapper.success(SliceResponse.from(result)));
    }

    @Override
    @GetMapping("/{crewId}")
    public ResponseEntity<ResponseWrapper<CrewResponse>> getCrewDetail(
        @PathVariable Long crewId
    ) {
        CrewResponse response = crewService.getCrewDetail(crewId);
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Override
    @PatchMapping("/{crewId}")
    public ResponseEntity<ResponseWrapper<CrewResponse>> updateCrew(
        @AuthenticationPrincipal AuthUser user,
        @PathVariable Long crewId,
        @Valid @RequestBody CrewUpdateRequest request
    ) {
        CrewResponse response = crewService.updateCrew(user.userId(), crewId, request);
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Override
    @DeleteMapping("/{crewId}")
    public ResponseEntity<ResponseWrapper<String>> deleteCrew(
        @AuthenticationPrincipal AuthUser user,
        @PathVariable Long crewId
    ) {
        crewService.deleteCrew(user.userId(), crewId);
        return ResponseEntity.ok(ResponseWrapper.success("크루가 삭제되었습니다."));
    }

    @Override
    @GetMapping("/{crewId}/members")
    public ResponseEntity<ResponseWrapper<CrewMembersResponse>> getCrewMembers(
        @PathVariable Long crewId,
        @RequestParam(required = false) String role
    ) {
        CrewMembersResponse response = crewService.getCrewMembers(crewId, role);
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Override
    @GetMapping("/{crewId}/members/count")
    public ResponseEntity<ResponseWrapper<MemberCountResponse>> getMemberCount(
        @PathVariable Long crewId
    ) {
        MemberCountResponse response = crewService.getMemberCount(crewId);
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Override
    @GetMapping("/{crewId}/members/{userId}/role")
    public ResponseEntity<ResponseWrapper<MemberRoleResponse>> getMemberRole(
        @PathVariable Long crewId,
        @PathVariable Long userId
    ) {
        MemberRoleResponse response = crewService.getMemberRole(crewId, userId);
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Override
    @PatchMapping("/{crewId}/leader")
    public ResponseEntity<ResponseWrapper<LeaderChangeResponse>> changeLeader(
        @AuthenticationPrincipal AuthUser user,
        @PathVariable Long crewId,
        @Valid @RequestBody LeaderChangeRequest request
    ) {
        LeaderChangeResponse response = crewService.changeLeader(user.userId(), crewId, request);
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Override
    @PatchMapping("/{crewId}/members/{userId}/role")
    public ResponseEntity<ResponseWrapper<RoleChangeResponse>> changeRole(
        @AuthenticationPrincipal AuthUser user,
        @PathVariable Long crewId,
        @PathVariable Long userId,
        @Valid @RequestBody RoleChangeRequest request
    ) {
        RoleChangeResponse response = crewService.changeRole(user.userId(), crewId, userId, request);
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Override
    @DeleteMapping("/{crewId}/members/{userId}")
    public ResponseEntity<ResponseWrapper<String>> kickMember(
        @AuthenticationPrincipal AuthUser user,
        @PathVariable Long crewId,
        @PathVariable Long userId
    ) {
        crewService.kickMember(user.userId(), crewId, userId);
        return ResponseEntity.ok(ResponseWrapper.success("해당 사용자가 크루에서 제거되었습니다."));
    }

    // === Membership API ===

    @Override
    @PostMapping("/{crewId}/join")
    public ResponseEntity<ResponseWrapper<MembershipResponse>> joinCrew(
        @AuthenticationPrincipal AuthUser user,
        @PathVariable Long crewId
    ) {
        MembershipResponse response = crewService.joinCrew(user.userId(), crewId);
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Override
    @DeleteMapping("/{crewId}/leave")
    public ResponseEntity<ResponseWrapper<String>> leaveCrew(
        @AuthenticationPrincipal AuthUser user,
        @PathVariable Long crewId
    ) {
        crewService.leaveCrew(user.userId(), crewId);
        return ResponseEntity.ok(ResponseWrapper.success("크루를 탈퇴했습니다."));
    }
}
