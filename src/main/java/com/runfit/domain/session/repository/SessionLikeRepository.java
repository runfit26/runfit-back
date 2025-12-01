package com.runfit.domain.session.repository;

import com.runfit.domain.session.entity.Session;
import com.runfit.domain.session.entity.SessionLike;
import com.runfit.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionLikeRepository extends JpaRepository<SessionLike, Long>, SessionLikeRepositoryCustom {

    boolean existsBySessionAndUser(Session session, User user);

    Optional<SessionLike> findBySessionAndUser(Session session, User user);

    boolean existsBySessionIdAndUserUserId(Long sessionId, Long userId);
}
