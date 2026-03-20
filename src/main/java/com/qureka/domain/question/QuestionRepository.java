package com.qureka.domain.question;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<UserQuestion, Long> {

    List<UserQuestion> findByUserUserIndexOrderByCreatedAtDesc(Long userIndex);

    @Query("""
        SELECT q FROM UserQuestion q
        WHERE q.user.userIndex = :userId
          AND (:query IS NULL OR LOWER(q.fileName) LIKE LOWER(CONCAT('%',:query,'%'))
               OR LOWER(q.questionName) LIKE LOWER(CONCAT('%',:query,'%')))
          AND (:type IS NULL OR q.questionType = :type)
        ORDER BY q.createdAt DESC
        """)
    List<UserQuestion> searchByUserId(@Param("userId") Long userId,
                                      @Param("query")  String query,
                                      @Param("type")   QuestionType type);
}
