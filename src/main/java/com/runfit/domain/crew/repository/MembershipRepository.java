package com.runfit.domain.crew.repository;

import com.runfit.domain.crew.entity.CrewRole;
import com.runfit.domain.crew.entity.Membership;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MembershipRepository extends JpaRepository<Membership, Long>, MembershipRepositoryCustom {

    boolean existsByUserUserIdAndCrewId(Long userId, Long crewId);

    Optional<Membership> findByUserUserIdAndCrewId(Long userId, Long crewId);

    @Query("SELECT m FROM Membership m JOIN FETCH m.user WHERE m.crew.id = :crewId")
    List<Membership> findAllByCrewIdWithUser(@Param("crewId") Long crewId);

    @Query("SELECT m FROM Membership m JOIN FETCH m.user WHERE m.crew.id = :crewId AND m.role = :role")
    List<Membership> findAllByCrewIdAndRoleWithUser(@Param("crewId") Long crewId, @Param("role") CrewRole role);

    @Query("SELECT m FROM Membership m WHERE m.crew.id = :crewId AND m.role = :role")
    Optional<Membership> findByCrewIdAndRole(@Param("crewId") Long crewId, @Param("role") CrewRole role);

    @Query("SELECT COUNT(m) FROM Membership m WHERE m.crew.id = :crewId AND m.role = :role")
    long countByCrewIdAndRole(@Param("crewId") Long crewId, @Param("role") CrewRole role);

    @Query("SELECT COUNT(m) FROM Membership m WHERE m.crew.id = :crewId")
    long countByCrewId(@Param("crewId") Long crewId);

    void deleteByUserUserIdAndCrewId(Long userId, Long crewId);
}
