package com.qureka.domain.question;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qureka.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "user_questions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "selection_id")
    @JsonProperty("selection_id")
    private Long selectionId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "file_name", length = 255, nullable = false)
    @JsonProperty("file_name")
    private String fileName;

    @Column(name = "question_name", length = 255, nullable = false)
    @Builder.Default
    @JsonProperty("question_name")
    private String questionName = "Untitled Question";

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", columnDefinition = "question_type_enum", nullable = false)
    @JsonProperty("question_type")
    private QuestionType questionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "question_data", columnDefinition = "jsonb", nullable = false)
    @Builder.Default
    @JsonProperty("question_data")
    private Map<String, Object> questionData = Map.of();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}