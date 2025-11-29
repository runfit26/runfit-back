package com.runfit.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runfit.global.jwt.JwtValidator;
import com.runfit.global.jwt.filter.JwtAuthenticationFilter;
import com.runfit.global.jwt.filter.JwtExceptionFilter;
import com.runfit.global.jwt.provider.JwtProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtFilterConfig {

    private final ObjectMapper objectMapper;
    private final JwtProvider accessTokenProvider;
    private final JwtValidator jwtTokenValidator;

    JwtFilterConfig(
        ObjectMapper objectMapper,
        @Qualifier("accessTokenProvider") JwtProvider accessTokenProvider,
        JwtValidator jwtTokenValidator
    ) {
        this.objectMapper = objectMapper;
        this.accessTokenProvider = accessTokenProvider;
        this.jwtTokenValidator = jwtTokenValidator;
    }

    @Bean
    public JwtExceptionFilter jwtExceptionFilter() {
        return new JwtExceptionFilter(objectMapper);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(accessTokenProvider, jwtTokenValidator);
    }
}
