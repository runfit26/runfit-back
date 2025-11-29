package com.runfit.domain.auth.Entity;

import com.runfit.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "blacklist_token")
public class BlacklistToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blacklist_token_id", nullable = false)
    private Long id;

    @Column(name = "token", nullable = false)
    private String token;

    public static BlacklistToken create(String token) {
        BlacklistToken blacklistToken = new BlacklistToken();
        blacklistToken.token = token;
        return blacklistToken;
    }
}
