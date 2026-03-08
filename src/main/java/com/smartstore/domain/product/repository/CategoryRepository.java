// CategoryRepository.java
package com.smartstore.domain.product.repository;

import com.smartstore.domain.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    boolean existsBySlug(String slug);
    Optional<Category> findBySlug(String slug);
    List<Category> findByStoreIdAndParentIsNull(UUID storeId); // Lấy root categories
}