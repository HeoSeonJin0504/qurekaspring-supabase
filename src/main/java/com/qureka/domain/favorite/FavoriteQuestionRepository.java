package com.qureka.domain.favorite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteQuestionRepository extends JpaRepository<FavoriteQuestion, Long> {

    List<FavoriteQuestion> findByUserUserIndexOrderByCreatedAtDesc(Long userIndex);
    List<FavoriteQuestion> findByFolderFolderIdAndUserUserIndex(Long folderId, Long userIndex);

    Optional<FavoriteQuestion> findByUserUserIndexAndQuestionSelectionIdAndQuestionIndex(
            Long userIndex, Long questionId, Short questionIndex);

    boolean existsByUserUserIndexAndQuestionSelectionIdAndQuestionIndex(
            Long userIndex, Long questionId, Short questionIndex);

    @Query("SELECT fq FROM FavoriteQuestion fq WHERE fq.user.userIndex = :userId AND fq.question.selectionId IN :ids")
    List<FavoriteQuestion> findByUserIndexAndQuestionIds(@Param("userId") Long userId,
                                                         @Param("ids") List<Long> ids);
}
