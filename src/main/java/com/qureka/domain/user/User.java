package com.qureka.domain.user;

import com.qureka.domain.auth.RefreshToken;
import com.qureka.domain.favorite.FavoriteFolder;
import com.qureka.domain.question.UserQuestion;
import com.qureka.domain.summary.UserSummary;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userindex")
    private Long userIndex;

    @Column(name = "userid",   length = 20,  nullable = false, unique = true)
    private String userid;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "name",     length = 50,  nullable = false)
    private String name;

    @Column(name = "age",      nullable = false)
    private Short age;

    @Column(name = "gender",   length = 10,  nullable = false)
    private String gender;

    @Column(name = "phone",    length = 20,  nullable = false, unique = true)
    private String phone;

    @Column(name = "email",    length = 100, unique = true)
    private String email;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserSummary> summaries = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FavoriteFolder> favoriteFolders = new ArrayList<>();
}
