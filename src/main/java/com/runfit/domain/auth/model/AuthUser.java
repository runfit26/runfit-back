package com.runfit.domain.auth.model;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public record AuthUser(
    Long userId,
    String username,
    String role
) {

    public static AuthUser create(Long userId, String username, String role) {
        return new AuthUser(userId, username, role);
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
