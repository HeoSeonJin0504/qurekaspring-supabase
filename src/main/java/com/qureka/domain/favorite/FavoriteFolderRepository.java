package com.qureka.domain.favorite;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FavoriteFolderRepository extends JpaRepository<FavoriteFolder, Long> {
    List<FavoriteFolder> findByUserUserIndexOrderByCreatedAtAsc(Long userIndex);
    Optional<FavoriteFolder> findByUserUserIndexAndFolderName(Long userIndex, String folderName);
    boolean existsByUserUserIndexAndFolderName(Long userIndex, String folderName);
}
