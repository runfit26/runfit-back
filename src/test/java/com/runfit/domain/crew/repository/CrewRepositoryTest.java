package com.runfit.domain.crew.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.runfit.domain.crew.entity.Crew;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.runfit.global.config.AuditConfig;
import com.runfit.global.config.QueryDslConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({QueryDslConfig.class, AuditConfig.class})
class CrewRepositoryTest {

    @Autowired
    private CrewRepository crewRepository;

    @Test
    @DisplayName("크루 저장 및 조회 성공")
    void save_and_find_success() {
        // given
        Crew crew = Crew.create("테스트 크루", "설명", "서울", null);

        // when
        Crew saved = crewRepository.save(crew);
        Optional<Crew> found = crewRepository.findById(saved.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("테스트 크루");
    }

    @Test
    @DisplayName("삭제되지 않은 크루 조회 성공")
    void findByIdAndDeletedIsNull_success() {
        // given
        Crew crew = Crew.create("테스트 크루", "설명", "서울", null);
        Crew saved = crewRepository.save(crew);

        // when
        Optional<Crew> found = crewRepository.findByIdAndDeletedIsNull(saved.getId());

        // then
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("삭제된 크루는 findByIdAndDeletedIsNull로 조회 안됨")
    void findByIdAndDeletedIsNull_deleted_notFound() {
        // given
        Crew crew = Crew.create("테스트 크루", "설명", "서울", null);
        Crew saved = crewRepository.save(crew);
        saved.delete();
        crewRepository.save(saved);

        // when
        Optional<Crew> found = crewRepository.findByIdAndDeletedIsNull(saved.getId());

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("크루 존재 여부 확인 - 존재하는 경우")
    void existsByIdAndDeletedIsNull_exists() {
        // given
        Crew crew = Crew.create("테스트 크루", "설명", "서울", null);
        Crew saved = crewRepository.save(crew);

        // when
        boolean exists = crewRepository.existsByIdAndDeletedIsNull(saved.getId());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("크루 존재 여부 확인 - 삭제된 경우")
    void existsByIdAndDeletedIsNull_deleted() {
        // given
        Crew crew = Crew.create("테스트 크루", "설명", "서울", null);
        Crew saved = crewRepository.save(crew);
        saved.delete();
        crewRepository.save(saved);

        // when
        boolean exists = crewRepository.existsByIdAndDeletedIsNull(saved.getId());

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional 반환")
    void findByIdAndDeletedIsNull_notExists() {
        // when
        Optional<Crew> found = crewRepository.findByIdAndDeletedIsNull(999L);

        // then
        assertThat(found).isEmpty();
    }
}
