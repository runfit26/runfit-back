package com.runfit.domain.user.entity;

import com.runfit.common.model.SoftDeleteEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends SoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "image")
    private String image;

    @Column(name = "introduction", length = 500)
    private String introduction;

    @Column(name = "city")
    private String city;

    @Column(name = "pace")
    private Integer pace; // 단위: second

    @ElementCollection
    @CollectionTable(
        name = "user_runnin_styles",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "style")
    private List<String> styles = new ArrayList<>();

    public static User create(String email, String password, String name) {
        User user = new User();
        user.name = name;
        user.email = email;
        user.password = password;
        return user;
    }

    public void update(String name, String image, String introduction, String city, Integer pace, List<String> styles) {
        if (name != null) {
            this.name = name;
        }
        if (image != null) {
            this.image = image;
        }
        if (introduction != null) {
            this.introduction = introduction;
        }
        if (city != null) {
            this.city = city;
        }
        if (pace != null) {
            this.pace = pace;
        }
        if (styles != null) {
            this.styles = new ArrayList<>(styles);
        }
    }
}
