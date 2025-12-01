package com.runfit.domain.user.controller;

import com.runfit.common.response.ResponseWrapper;
import com.runfit.common.response.SliceResponse;
import com.runfit.domain.auth.model.AuthUser;
import com.runfit.domain.user.controller.dto.response.LikedSessionResponse;
import com.runfit.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

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
}
