package com.qureka.domain.summary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SummaryRepository extends JpaRepository<UserSummary, Long> {

    List<UserSummary> findByUserUserIndexOrderByCreatedAtDesc(Long userIndex);

    @Query("""
        SELECT s FROM UserSummary s
        WHERE s.user.userIndex = :userId
          AND (:query IS NULL OR LOWER(s.fileName) LIKE LOWER(CONCAT('%',:query,'%'))
               OR LOWER(s.summaryName) LIKE LOWER(CONCAT('%',:query,'%')))
          AND (:type IS NULL OR s.summaryType = :type)
        ORDER BY s.createdAt DESC
        """)
    List<UserSummary> searchByUserId(@Param("userId") Long userId,
                                     @Param("query")  String query,
                                     @Param("type")   SummaryType type);

    @Query("""
        SELECT s.selectionId, s.fileName, s.summaryName, s.summaryType, s.createdAt
        FROM UserSummary s WHERE s.user.userIndex = :userId ORDER BY s.createdAt DESC
        """)
    List<Object[]> findMetaByUserId(@Param("userId") Long userId);
}
