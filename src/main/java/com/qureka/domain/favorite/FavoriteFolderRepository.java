package com.qureka.domain.favorite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteFolderRepository extends JpaRepository<FavoriteFolder, Long> {

    List<FavoriteFolder> findByUserUserIndexOrderByCreatedAtAsc(Long userIndex);

    Optional<FavoriteFolder> findByUserUserIndexAndFolderName(Long userIndex, String folderName);

    boolean existsByUserUserIndexAndFolderName(Long userIndex, String folderName);

    /** 폴더별 즐겨찾기 문제 개수 조회 */
    @Query("SELECT COUNT(fq) FROM FavoriteQuestion fq WHERE fq.folder.folderId = :folderId")
    long countQuestionsByFolderId(@Param("folderId") Long folderId);
}