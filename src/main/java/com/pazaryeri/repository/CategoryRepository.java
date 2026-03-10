package com.pazaryeri.repository;

import com.pazaryeri.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByParentIsNullAndActiveTrueOrderBySortOrderAsc();
    List<Category> findByParentIdAndActiveTrueOrderBySortOrderAsc(Long parentId);
    boolean existsBySlug(String slug);
}
