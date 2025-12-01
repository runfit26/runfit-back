package com.runfit.domain.crew.repository;

import com.runfit.domain.crew.controller.dto.request.CrewSearchCondition;
import com.runfit.domain.crew.controller.dto.response.CrewListResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface CrewRepositoryCustom {

    Slice<CrewListResponse> searchCrews(CrewSearchCondition condition, Pageable pageable);
}
