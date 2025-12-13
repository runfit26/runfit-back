package com.runfit.domain.user.controller;

import com.runfit.common.response.PageResponse;
import com.runfit.common.response.ResponseWrapper;
import com.runfit.common.response.SliceResponse;
import com.runfit.domain.auth.model.AuthUser;
import com.runfit.domain.crew.controller.dto.response.CrewListResponse;
import com.runfit.domain.review.controller.dto.response.ReviewResponse;
import com.runfit.domain.session.controller.dto.response.SessionListResponse;
import com.runfit.domain.user.controller.dto.request.UserUpdateRequest;
import com.runfit.domain.user.controller.dto.response.LikedSessionResponse;
import com.runfit.domain.user.controller.dto.response.MyCrewResponse;
import com.runfit.domain.user.controller.dto.response.UserProfileResponse;
import com.runfit.domain.user.controller.dto.response.UserResponse;
import com.runfit.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    @GetMapping
    public ResponseEntity<ResponseWrapper<UserResponse>> getMyInfo(
        @AuthenticationPrincipal AuthUser user
    ) {
        UserResponse response = userService.getMyInfo(user.userId());
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Override
    @PatchMapping
    public ResponseEntity<ResponseWrapper<UserResponse>> updateMyInfo(
        @AuthenticationPrincipal AuthUser user,
        @Valid @RequestBody UserUpdateRequest request
    ) {
        UserResponse response = userService.updateMyInfo(user.userId(), request);
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Override
    @GetMapping("/{userId}")
    public ResponseEntity<ResponseWrapper<UserProfileResponse>> getUserProfile(
        @AuthenticationPrincipal AuthUser user,
        @PathVariable Long userId
    ) {
        UserProfileResponse response = userService.getUserProfile(userId);
        return ResponseEntity.ok(ResponseWrapper.success(response));
    }

    @Override
    @GetMapping("/me/reviews")
    public ResponseEntity<ResponseWrapper<PageResponse<ReviewResponse>>> getMyReviews(
        @AuthenticationPrincipal AuthUser user,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "4") int size
    ) {
        Page<ReviewResponse> result = userService.getMyReviews(user.userId(), PageRequest.of(page, size));
        return ResponseEntity.ok(ResponseWrapper.success(PageResponse.from(result)));
    }

    @Override
    @GetMapping("/me/likes")
    public ResponseEntity<ResponseWrapper<SliceResponse<LikedSessionResponse>>> getMyLikedSessions(
        @AuthenticationPrincipal AuthUser user,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Slice<LikedSessionResponse> result = userService.getMyLikedSessions(user.userId(), PageRequest.of(page, size));
        return ResponseEntity.ok(ResponseWrapper.success(SliceResponse.from(result)));
    }

    @Override
    @GetMapping("/me/sessions")
    public ResponseEntity<ResponseWrapper<SliceResponse<SessionListResponse>>> getMyHostedSessions(
        @AuthenticationPrincipal AuthUser user,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Slice<SessionListResponse> result = userService.getMyHostedSessions(user.userId(), PageRequest.of(page, size));
        return ResponseEntity.ok(ResponseWrapper.success(SliceResponse.from(result)));
    }

    @Override
    @GetMapping("/me/crews/owned")
    public ResponseEntity<ResponseWrapper<SliceResponse<CrewListResponse>>> getMyOwnedCrews(
        @AuthenticationPrincipal AuthUser user,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Slice<CrewListResponse> result = userService.getMyOwnedCrews(user.userId(), PageRequest.of(page, size));
        return ResponseEntity.ok(ResponseWrapper.success(SliceResponse.from(result)));
    }

    @Override
    @GetMapping("/me/crews")
    public ResponseEntity<ResponseWrapper<SliceResponse<MyCrewResponse>>> getMyCrews(
        @AuthenticationPrincipal AuthUser user,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Slice<MyCrewResponse> result = userService.getMyCrews(user.userId(), PageRequest.of(page, size));
        return ResponseEntity.ok(ResponseWrapper.success(SliceResponse.from(result)));
    }

    @Override
    @GetMapping("/me/sessions/participating")
    public ResponseEntity<ResponseWrapper<SliceResponse<SessionListResponse>>> getMyParticipatingSessions(
        @AuthenticationPrincipal AuthUser user,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String status
    ) {
        Slice<SessionListResponse> result = userService.getMyParticipatingSessions(user.userId(), status, PageRequest.of(page, size));
        return ResponseEntity.ok(ResponseWrapper.success(SliceResponse.from(result)));
    }
}
