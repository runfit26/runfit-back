package com.runfit.domain.session.controller;

import com.runfit.common.response.ResponseWrapper;
import com.runfit.common.response.SliceResponse;
import com.runfit.domain.auth.model.AuthUser;
import com.runfit.domain.session.controller.dto.request.SessionCreateRequest;
import com.runfit.domain.session.controller.dto.request.SessionSearchCondition;
import com.runfit.domain.session.controller.dto.request.SessionUpdateRequest;
import com.runfit.domain.session.controller.dto.response.SessionDetailResponse;
import com.runfit.domain.session.controller.dto.response.SessionJoinResponse;
import com.runfit.domain.session.controller.dto.response.SessionLikeResponse;
import com.runfit.domain.session.controller.dto.response.SessionListResponse;
import com.runfit.domain.session.controller.dto.response.SessionParticipantsResponse;
import com.runfit.domain.session.controller.dto.response.SessionResponse;
import com.runfit.domain.session.entity.SessionLevel;
import com.runfit.domain.session.service.SessionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
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
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController implements SessionApi {

    private final SessionService sessionService;

    @Override
    @PostMapping
    public ResponseEntity<ResponseWrapper<SessionResponse>> createSession(
        @AuthenticationPrincipal AuthUser user,
        @Valid @RequestBody SessionCreateRequest request
    ) {
        SessionResponse response = sessionService.createSession(user.userId(), request);
        URI location = URI.create("/api/sessions/" + response.id());
        return ResponseEntity.created(location).body(ResponseWrapper.success(response));
    }

    @Override
    @GetMapping
    public ResponseEntity<ResponseWrapper<SliceResponse<SessionListResponse>>> searchSessions(
        @AuthenticationPrincipal AuthUser user,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) List<String> city,
        @RequestParam(required = false) List<String> district,
        @RequestParam(required = false) Long crewId,
        @RequestParam(required = false) SessionLevel level,
        @RequestParam(required = false) LocalDate dateFrom,
        @RequestParam(required = false) LocalDate dateTo,
        @RequestParam(required = false) LocalTime timeFrom,
        @RequestParam(required = false) LocalTime timeTo,
        @RequestParam(required = false) String sort
    ) {
        SessionSearchCondition condition = SessionSearchCondition.of(
            city, district, crewId, level, dateFrom, dateTo, timeFrom, timeTo, sort
        );
        Long userId = user != null ? user.userId() : null;
        Slice<SessionListResponse> result = sessionService.searchSessions(condition, userId, PageRequest.of(page, size));
        return ResponseEntity.ok(ResponseWrapper.success(SliceResponse.from(result)));
    }

    @Override
    @GetMapping("/{sessionId}")
    public ResponseEntity<ResponseWrapper<SessionDetailResponse>> getSessionDetail(
        @AuthenticationPrincipal AuthUser user,
        @PathVariable Long sessionId
    ) {
        Long userId = user != null ? user.userId() : null;
        SessionDetailResponse response = sessionService.getSessionDetail(sessionId, userId);
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Override
    @PostMapping("/{sessionId}/join")
    public ResponseEntity<ResponseWrapper<SessionJoinResponse>> joinSession(
        @AuthenticationPrincipal AuthUser user,
        @PathVariable Long sessionId
    ) {
        SessionJoinResponse response = sessionService.joinSession(user.userId(), sessionId);
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Override
    @DeleteMapping("/{sessionId}/join")
    public ResponseEntity<ResponseWrapper<SessionJoinResponse>> cancelJoinSession(
        @AuthenticationPrincipal AuthUser user,
        @PathVariable Long sessionId
    ) {
        SessionJoinResponse response = sessionService.cancelJoinSession(user.userId(), sessionId);
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Override
    @PostMapping("/{sessionId}/like")
    public ResponseEntity<ResponseWrapper<SessionLikeResponse>> likeSession(
        @AuthenticationPrincipal AuthUser user,
        @PathVariable Long sessionId
    ) {
        SessionLikeResponse response = sessionService.likeSession(user.userId(), sessionId);
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Override
    @DeleteMapping("/{sessionId}/like")
    public ResponseEntity<ResponseWrapper<SessionLikeResponse>> unlikeSession(
        @AuthenticationPrincipal AuthUser user,
        @PathVariable Long sessionId
    ) {
        SessionLikeResponse response = sessionService.unlikeSession(user.userId(), sessionId);
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Override
    @GetMapping("/{sessionId}/participants")
    public ResponseEntity<ResponseWrapper<SessionParticipantsResponse>> getSessionParticipants(
        @PathVariable Long sessionId
    ) {
        SessionParticipantsResponse response = sessionService.getSessionParticipants(sessionId);
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Override
    @PatchMapping("/{sessionId}")
    public ResponseEntity<ResponseWrapper<SessionResponse>> updateSession(
        @AuthenticationPrincipal AuthUser user,
        @PathVariable Long sessionId,
        @Valid @RequestBody SessionUpdateRequest request
    ) {
        SessionResponse response = sessionService.updateSession(user.userId(), sessionId, request);
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Override
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ResponseWrapper<Map<String, String>>> deleteSession(
        @AuthenticationPrincipal AuthUser user,
        @PathVariable Long sessionId
    ) {
        sessionService.deleteSession(user.userId(), sessionId);
        return ResponseEntity.ok(ResponseWrapper.success(Map.of("message", "세션이 삭제되었습니다.")));
    }
}
