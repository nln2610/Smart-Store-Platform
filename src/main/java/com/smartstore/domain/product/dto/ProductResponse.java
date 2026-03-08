// ProductResponse.java
package com.smartstore.domain.product.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ProductResponse {
    private UUID id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer stockQuantity;
    private Integer minStockLevel;
    private boolean lowStock;        // ⭐ Computed field — frontend không cần tự tính
    private String imageUrl;
    private Boolean isActive;
    private UUID storeId;
    private String storeName;
    private UUID categoryId;
    private String categoryName;
    private Instant createdAt;
    private Instant updatedAt;
}