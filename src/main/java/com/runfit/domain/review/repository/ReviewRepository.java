package com.runfit.domain.review.repository;

import com.runfit.domain.review.entity.Review;
import com.runfit.domain.session.entity.Session;
import com.runfit.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {

    boolean existsBySessionAndUser(Session session, User user);

    @Query("SELECT r FROM Review r " +
           "JOIN FETCH r.session s " +
           "JOIN FETCH s.crew " +
           "JOIN FETCH r.user " +
           "WHERE r.id = :reviewId")
    Optional<Review> findByIdWithSessionAndUser(@Param("reviewId") Long reviewId);
}
