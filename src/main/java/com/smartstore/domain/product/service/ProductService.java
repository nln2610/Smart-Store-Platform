// ProductService.java (interface)
package com.smartstore.domain.product.service;

import com.smartstore.domain.product.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {
    ProductResponse create(CreateProductRequest request);
    ProductResponse getById(UUID id);
    Page<ProductResponse> search(UUID storeId, String keyword, UUID categoryId, Pageable pageable);
    ProductResponse update(UUID id, UpdateProductRequest request);
    void delete(UUID id);
    Page<ProductResponse> getLowStockProducts(UUID storeId, Pageable pageable);
}