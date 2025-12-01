package com.runfit.domain.crew.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CrewTest {

    @Test
    @DisplayName("크루 생성 성공")
    void create_success() {
        // given
        String name = "잠실 러닝 크루";
        String description = "잠실에서 함께 달리는 크루입니다.";
        String region = "서울";
        String image = "https://example.com/crew.jpg";

        // when
        Crew crew = Crew.create(name, description, region, image);

        // then
        assertThat(crew.getName()).isEqualTo(name);
        assertThat(crew.getDescription()).isEqualTo(description);
        assertThat(crew.getRegion()).isEqualTo(region);
        assertThat(crew.getImage()).isEqualTo(image);
        assertThat(crew.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("크루 정보 수정 성공")
    void update_success() {
        // given
        Crew crew = Crew.create("원래 이름", "원래 설명", "서울", "https://old.jpg");

        String newName = "새 이름";
        String newDescription = "새 설명";
        String newRegion = "부산";
        String newImage = "https://new.jpg";

        // when
        crew.update(newName, newDescription, newRegion, newImage);

        // then
        assertThat(crew.getName()).isEqualTo(newName);
        assertThat(crew.getDescription()).isEqualTo(newDescription);
        assertThat(crew.getRegion()).isEqualTo(newRegion);
        assertThat(crew.getImage()).isEqualTo(newImage);
    }

    @Test
    @DisplayName("크루 삭제 (Soft Delete) 성공")
    void delete_success() {
        // given
        Crew crew = Crew.create("크루", "설명", "서울", null);
        assertThat(crew.isDeleted()).isFalse();

        // when
        crew.delete();

        // then
        assertThat(crew.isDeleted()).isTrue();
    }
}
