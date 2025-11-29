package com.runfit.domain.auth.controller;

import com.runfit.common.response.ResponseWrapper;
import com.runfit.domain.auth.controller.dto.request.SignInRequest;
import com.runfit.domain.auth.controller.dto.request.SignUpRequest;
import com.runfit.domain.auth.controller.dto.response.SignInResponse;
import com.runfit.domain.auth.model.AuthUser;
import com.runfit.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ResponseWrapper<String>> signUp(@RequestBody SignUpRequest request) {
        Long userId = authService.signUp(request);

        URI location = URI.create("/users/" + userId);

        return ResponseEntity
            .created(location)
            .body(ResponseWrapper.success("사용자 생성 성공"));
    }

    @PostMapping("/signin")
    public ResponseEntity<ResponseWrapper<SignInResponse>> signIn(@RequestBody SignInRequest request) {
        String token = authService.signIn(request);

        return ResponseEntity
            .ok()
            .body(ResponseWrapper.success(SignInResponse.from(token)));
    }

    @PostMapping("/signout")
    public ResponseEntity<ResponseWrapper<String>> signOut(@AuthenticationPrincipal AuthUser user, HttpServletRequest request) {
        authService.signOut(request);

        return ResponseEntity
            .ok()
            .body(ResponseWrapper.success("로그아웃 성공"));
    }
}
