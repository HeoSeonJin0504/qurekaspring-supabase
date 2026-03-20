package com.qureka.domain.favorite.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.qureka.domain.favorite.FavoriteQuestion;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 프론트 transformQuestionItem()이 기대하는 snake_case flat 구조
 *   item.question_id    → QuestionItem.id
 *   item.file_name      → name / displayName
 *   item.question_type  → displayType
 *   item.question_text  → text / rawJson  (question_data JSON 문자열)
 *   item.favorite_id    → favoriteId
 *   item.folder_id      → folderId
 *   item.question_index → questionIndex
 *   item.created_at     → date / time
 */
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

    /** question_data Map을 JSON 문자열로 변환 — 프론트가 item.question_text 로 파싱 */
    @JsonProperty("question_data")
    private final Map<String, Object> questionData;

    @JsonProperty("question_index")
    private final Short questionIndex;

    @JsonProperty("created_at")
    private final OffsetDateTime createdAt;

    @JsonProperty("question_created_at")
    private final OffsetDateTime questionCreatedAt;

    public FavoriteQuestionResponse(FavoriteQuestion fq) {
        this.favoriteId    = fq.getFavoriteId();
        this.folderId      = fq.getFolder() != null ? fq.getFolder().getFolderId()   : null;
        this.folderName    = fq.getFolder() != null ? fq.getFolder().getFolderName() : null;
        this.questionIndex = fq.getQuestionIndex();
        this.createdAt     = fq.getCreatedAt();

        var q = fq.getQuestion();
        if (q != null) {
            this.questionId        = q.getSelectionId();
            this.fileName          = q.getFileName();
            this.questionName      = q.getQuestionName();
            this.questionType      = q.getQuestionType() != null ? q.getQuestionType().name() : null;
            this.questionData      = q.getQuestionData();
            this.questionCreatedAt = q.getCreatedAt();
        } else {
            this.questionId        = null;
            this.fileName          = null;
            this.questionName      = null;
            this.questionType      = null;
            this.questionData      = null;
            this.questionCreatedAt = null;
        }
    }
}