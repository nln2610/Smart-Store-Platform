// UpdateProductRequest.java
package com.smartstore.domain.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdateProductRequest {
    // ⭐ Tất cả field đều Optional khi update — chỉ update field nào có giá trị

    @Size(max = 255)
    private String name;

    private String description;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @DecimalMin(value = "0.0")
    private BigDecimal costPrice;

    @Min(value = 0)
    private Integer minStockLevel;

    private String imageUrl;

    private UUID categoryId;

    private Boolean isActive;
}