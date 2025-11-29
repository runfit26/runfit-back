package com.runfit.domain.auth.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignInRequest(
    @NotBlank(message = "이메일은 필수 입니다")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "이메일 형식이 올바르지 않습니다")
    String email,

    @NotBlank(message = "패스워드는 필수 입니다")
    String password
) {

}
