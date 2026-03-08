// InventoryTransactionRepository.java
package com.smartstore.domain.inventory.repository;

import com.smartstore.domain.inventory.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {
    Page<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(UUID productId, Pageable pageable);
}