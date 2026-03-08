// CreateProductRequest.java
package com.smartstore.domain.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateProductRequest {

    @NotBlank(message = "SKU is required")
    @Size(max = 100)
    private String sku;

    @NotBlank(message = "Product name is required")
    @Size(max = 255)
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Cost price must be >= 0")
    private BigDecimal costPrice;

    @Min(value = 0, message = "Stock quantity must be >= 0")
    private Integer stockQuantity = 0;

    @Min(value = 0)
    private Integer minStockLevel = 10;

    private String imageUrl;

    @NotNull(message = "Store ID is required")
    private UUID storeId;

    private UUID categoryId;         // Optional
}