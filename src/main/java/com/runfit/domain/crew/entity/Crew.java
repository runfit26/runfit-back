package com.runfit.domain.crew.entity;

import com.runfit.common.model.SoftDeleteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "crews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Crew extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "region")
    private String region;

    @Column(name = "image")
    private String image;

    @Builder
    private Crew(String name, String description, String region, String image) {
        this.name = name;
        this.description = description;
        this.region = region;
        this.image = image;
    }

    public static Crew create(String name, String description, String region, String image) {
        return Crew.builder()
            .name(name)
            .description(description)
            .region(region)
            .image(image)
            .build();
    }

    public void update(String name, String description, String region, String image) {
        this.name = name;
        this.description = description;
        this.region = region;
        this.image = image;
    }
}
