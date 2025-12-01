package com.runfit.domain.user.service;

import com.runfit.domain.session.repository.SessionLikeRepository;
import com.runfit.domain.user.controller.dto.response.LikedSessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SessionLikeRepository sessionLikeRepository;

    @Transactional(readOnly = true)
    public Slice<LikedSessionResponse> getMyLikedSessions(Long userId, Pageable pageable) {
        return sessionLikeRepository.findLikedSessionsByUserId(userId, pageable);
    }
}
