package com.runfit.domain.auth.repository;

import com.runfit.domain.auth.Entity.BlacklistToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistTokenRepository extends JpaRepository<BlacklistToken, Long> {

    boolean existsByToken(String token);
}
