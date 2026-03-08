// ProductServiceImpl.java
package com.smartstore.domain.product.service;

import com.smartstore.common.exception.BusinessException;
import com.smartstore.common.exception.ResourceNotFoundException;
import com.smartstore.domain.product.dto.*;
import com.smartstore.domain.product.entity.Category;
import com.smartstore.domain.product.entity.Product;
import com.smartstore.domain.product.repository.CategoryRepository;
import com.smartstore.domain.product.repository.ProductRepository;
import com.smartstore.domain.store.entity.Store;
import com.smartstore.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// ⭐ readOnly = true mặc định cho toàn class — tối ưu performance cho các query
//    Chỉ method nào thay đổi data mới override bằng @Transactional riêng
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StoreRepository storeRepository;

    @Override
    @Transactional   // ⭐ Override readOnly = false khi cần ghi dữ liệu
    public ProductResponse create(CreateProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new BusinessException("SKU already exists: " + request.getSku());
        }

        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .costPrice(request.getCostPrice())
                .stockQuantity(request.getStockQuantity())
                .minStockLevel(request.getMinStockLevel())
                .imageUrl(request.getImageUrl())
                .store(store)
                .category(category)
                .build();

        return toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse getById(UUID id) {
        Product product = productRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        return toResponse(product);
    }

    @Override
    public Page<ProductResponse> search(
            UUID storeId, String keyword, UUID categoryId, Pageable pageable
    ) {
        return productRepository
                .searchProducts(storeId, keyword, categoryId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public ProductResponse update(UUID id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        // ⭐ Chỉ update field nào client gửi lên (không null)
        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getCostPrice() != null) product.setCostPrice(request.getCostPrice());
        if (request.getMinStockLevel() != null) product.setMinStockLevel(request.getMinStockLevel());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        if (request.getIsActive() != null) product.setIsActive(request.getIsActive());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            product.setCategory(category);
        }

        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        // ⭐ Soft delete — không xóa thật, chỉ đánh dấu isActive = false
        //    Lý do: đơn hàng cũ vẫn cần reference đến sản phẩm này
        product.setIsActive(false);
        productRepository.save(product);
    }

    @Override
    public Page<ProductResponse> getLowStockProducts(UUID storeId, Pageable pageable) {
        return productRepository.findLowStockProducts(storeId, pageable).map(this::toResponse);
    }

    // ⭐ Mapper tập trung 1 chỗ — dễ maintain hơn là rải rác khắp nơi
    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .sku(p.getSku())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .costPrice(p.getCostPrice())
                .stockQuantity(p.getStockQuantity())
                .minStockLevel(p.getMinStockLevel())
                .lowStock(p.isLowStock())
                .imageUrl(p.getImageUrl())
                .isActive(p.getIsActive())
                .storeId(p.getStore().getId())
                .storeName(p.getStore().getName())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}