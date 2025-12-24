package com.runfit.domain.session.repository;

import com.runfit.domain.session.entity.SessionParticipant;
import com.runfit.domain.user.controller.dto.response.ParticipatingSessionResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface SessionParticipantRepositoryCustom {

    Slice<ParticipatingSessionResponse> findParticipatingSessionsByUserId(
        Long userId, String status, Pageable pageable);

    List<SessionParticipant> findParticipantsBySessionIds(List<Long> sessionIds);
}
