package com.qureka.domain.favorite.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.qureka.domain.favorite.FavoriteFolder;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 *   { ...folder, question_count: parseInt(rows[0].count) }
 */
@Getter
public class FavoriteFolderResponse {

    @JsonProperty("folder_id")
    private final Long folderId;

    @JsonProperty("folder_name")
    private final String folderName;

    @JsonProperty("description")
    private final String description;

    @JsonProperty("created_at")
    private final OffsetDateTime createdAt;

    @JsonProperty("question_count")
    private final long questionCount;

    public FavoriteFolderResponse(FavoriteFolder folder, long questionCount) {
        this.folderId      = folder.getFolderId();
        this.folderName    = folder.getFolderName();
        this.description   = folder.getDescription();
        this.createdAt     = folder.getCreatedAt();
        this.questionCount = questionCount;
    }
}