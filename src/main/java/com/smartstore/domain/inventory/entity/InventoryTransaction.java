package com.smartstore.domain.inventory.entity;

import com.smartstore.common.entity.BaseEntity;
import com.smartstore.common.enums.TransactionType;
import com.smartstore.domain.product.entity.Product;
import com.smartstore.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "inventory_transactions",
        indexes = {
                @Index(name = "idx_inv_product_id", columnList = "product_id"),
                @Index(name = "idx_inv_created_at", columnList = "created_at")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;     // IMPORT, EXPORT, ADJUSTMENT, RETURN

    @Column(nullable = false)
    private Integer quantity;         // dương: nhập, âm: xuất

    @Column(name = "stock_before", nullable = false)
    private Integer stockBefore;      // ⭐ Lưu tồn kho TRƯỚC khi thay đổi
    //    để có thể audit/rollback sau này

    @Column(name = "stock_after", nullable = false)
    private Integer stockAfter;       // tồn kho SAU khi thay đổi

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "reference_id")
    private UUID referenceId;         // order_id nếu xuất kho do bán hàng

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;
}