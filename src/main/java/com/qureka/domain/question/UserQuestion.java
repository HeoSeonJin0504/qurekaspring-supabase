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
    @JsonIgnore   // question_data Map 자체는 숨기고 question_text로만 노출
    private Map<String, Object> questionData = Map.of();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    /**
     * question_data = {"question_text": "JSON문자열"} 형태로 저장되어 있으므로
     * question_data.question_text 값을 꺼내서 반환
     */
    @JsonProperty("question_text")
    public String getQuestionText() {
        if (questionData == null) return null;
        Object qt = questionData.get("question_text");
        return qt != null ? String.valueOf(qt) : null;
    }
}