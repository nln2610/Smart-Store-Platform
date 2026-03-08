package com.smartstore.domain.product.entity;

import com.smartstore.common.entity.BaseEntity;
import com.smartstore.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_products_sku", columnList = "sku"),
                @Index(name = "idx_products_store_id", columnList = "store_id"),
                @Index(name = "idx_products_category_id", columnList = "category_id")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "min_stock_level")
    @Builder.Default
    private Integer minStockLevel = 10;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // ===== BUSINESS METHODS =====

    public boolean isLowStock() {
        return this.stockQuantity <= this.minStockLevel;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        this.stockQuantity += quantity;
    }

    public void decreaseStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (this.stockQuantity < quantity) {
            throw new IllegalStateException(
                    "Insufficient stock for: " + this.name +
                            ". Available: " + this.stockQuantity +
                            ", Requested: " + quantity
            );
        }
        this.stockQuantity -= quantity;
    }
}