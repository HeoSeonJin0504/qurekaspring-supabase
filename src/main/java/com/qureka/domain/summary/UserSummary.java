package com.qureka.domain.summary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qureka.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_summaries")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserSummary {

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

    @Column(name = "summary_name", length = 255, nullable = false)
    @Builder.Default
    @JsonProperty("summary_name")
    private String summaryName = "Untitled Summary";

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_type", columnDefinition = "summary_type_enum", nullable = false)
    @JsonProperty("summary_type")
    private SummaryType summaryType;

    @Column(name = "summary_text", columnDefinition = "TEXT", nullable = false)
    @JsonProperty("summary_text")
    private String summaryText;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}