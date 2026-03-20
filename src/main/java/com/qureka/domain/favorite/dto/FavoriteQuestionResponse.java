package com.qureka.domain.favorite.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qureka.domain.favorite.FavoriteQuestion;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FavoriteQuestionResponse {

    @JsonProperty("favorite_id")
    private final Long favoriteId;

    @JsonProperty("folder_id")
    private final Long folderId;

    @JsonProperty("folder_name")
    private final String folderName;

    @JsonProperty("question_id")
    private final Long questionId;

    @JsonProperty("file_name")
    private final String fileName;

    @JsonProperty("question_name")
    private final String questionName;

    @JsonProperty("question_type")
    private final String questionType;

    /** question_text: r.question_data?.question_text || '{}' */
    @JsonProperty("question_text")
    private final String questionText;

    @JsonProperty("question_index")
    private final Short questionIndex;

    @JsonProperty("created_at")
    private final OffsetDateTime createdAt;

    @JsonProperty("favorited_at")
    private final OffsetDateTime favoritedAt;

    public FavoriteQuestionResponse(FavoriteQuestion fq) {
        this.favoriteId  = fq.getFavoriteId();
        this.folderId    = fq.getFolder() != null ? fq.getFolder().getFolderId()   : null;
        this.folderName  = fq.getFolder() != null ? fq.getFolder().getFolderName() : null;
        this.questionIndex = fq.getQuestionIndex();
        this.favoritedAt   = fq.getCreatedAt();   // Node.js: favorited_at = fq.created_at
        this.createdAt     = fq.getCreatedAt();

        var q = fq.getQuestion();
        if (q != null) {
            this.questionId   = q.getSelectionId();
            this.fileName     = q.getFileName();
            this.questionName = q.getQuestionName();
            this.questionType = q.getQuestionType() != null ? q.getQuestionType().name() : null;
            // Node.js: question_data?.question_text || '{}'
            var data = q.getQuestionData();
            Object qt = (data != null) ? data.get("question_text") : null;
            this.questionText = qt != null ? String.valueOf(qt) : "{}";
        } else {
            this.questionId   = null;
            this.fileName     = null;
            this.questionName = null;
            this.questionType = null;
            this.questionText = "{}";
        }
    }
}