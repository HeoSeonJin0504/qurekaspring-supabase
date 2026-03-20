package com.qureka.domain.favorite;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qureka.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "favorite_folders",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "folder_name"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FavoriteFolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id")
    @JsonProperty("folder_id")
    private Long folderId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "folder_name", length = 100, nullable = false)
    @JsonProperty("folder_name")
    private String folderName;

    @Column(name = "description", length = 255)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FavoriteQuestion> favoriteQuestions = new ArrayList<>();
}