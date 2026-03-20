package com.qureka.domain.favorite;

import com.qureka.domain.question.UserQuestion;
import com.qureka.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "favorite_questions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","question_id","question_index"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FavoriteQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Long favoriteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", nullable = false)
    private FavoriteFolder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private UserQuestion question;

    @Column(name = "question_index", nullable = false)
    @Builder.Default
    private Short questionIndex = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
