// StoreRepository.java
package com.smartstore.domain.store.repository;

import com.smartstore.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
}