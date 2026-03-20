package com.qureka.domain.favorite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteQuestionRepository extends JpaRepository<FavoriteQuestion, Long> {

    // question을 fetch join해서 LAZY 프록시 문제 방지 (Untitled 방지)
    @Query("SELECT fq FROM FavoriteQuestion fq JOIN FETCH fq.question WHERE fq.user.userIndex = :userIndex ORDER BY fq.createdAt DESC")
    List<FavoriteQuestion> findByUserUserIndexOrderByCreatedAtDesc(@Param("userIndex") Long userIndex);

    @Query("SELECT fq FROM FavoriteQuestion fq JOIN FETCH fq.question WHERE fq.folder.folderId = :folderId AND fq.user.userIndex = :userIndex")
    List<FavoriteQuestion> findByFolderFolderIdAndUserUserIndex(@Param("folderId") Long folderId,
                                                                @Param("userIndex") Long userIndex);

    Optional<FavoriteQuestion> findByUserUserIndexAndQuestionSelectionIdAndQuestionIndex(
            Long userIndex, Long questionId, Short questionIndex);

    boolean existsByUserUserIndexAndQuestionSelectionIdAndQuestionIndex(
            Long userIndex, Long questionId, Short questionIndex);

    // question fetch join으로 question 필드 즉시 로드
    @Query("SELECT fq FROM FavoriteQuestion fq JOIN FETCH fq.question WHERE fq.user.userIndex = :userId AND fq.question.selectionId IN :ids")
    List<FavoriteQuestion> findByUserIndexAndQuestionIds(@Param("userId") Long userId,
                                                         @Param("ids") List<Long> ids);
}