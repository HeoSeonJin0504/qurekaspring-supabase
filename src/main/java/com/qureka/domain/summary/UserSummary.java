package com.qureka.domain.summary;

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
    private Long selectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "file_name",    length = 255, nullable = false)
    private String fileName;

    @Column(name = "summary_name", length = 255, nullable = false)
    @Builder.Default
    private String summaryName = "Untitled Summary";

    @Enumerated(EnumType.STRING)
    @Column(name = "summary_type", columnDefinition = "summary_type_enum", nullable = false)
    private SummaryType summaryType;

    @Column(name = "summary_text", columnDefinition = "TEXT", nullable = false)
    private String summaryText;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
