package com.runfit.domain.crew.repository;

import com.runfit.domain.crew.entity.Crew;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrewRepository extends JpaRepository<Crew, Long>, CrewRepositoryCustom {

    Optional<Crew> findByIdAndDeletedIsNull(Long id);

    boolean existsByIdAndDeletedIsNull(Long id);
}
