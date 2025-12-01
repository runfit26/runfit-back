package com.runfit.domain.session.repository;

import com.runfit.domain.user.controller.dto.response.LikedSessionResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface SessionLikeRepositoryCustom {

    Slice<LikedSessionResponse> findLikedSessionsByUserId(Long userId, Pageable pageable);
}
