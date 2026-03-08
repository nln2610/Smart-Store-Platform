package com.smartstore.domain.order.entity;

import com.smartstore.common.entity.BaseEntity;
import com.smartstore.common.enums.OrderStatus;
import com.smartstore.common.enums.PaymentMethod;
import com.smartstore.common.enums.PaymentStatus;
import com.smartstore.domain.store.entity.Store;
import com.smartstore.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_customer_id", columnList = "customer_id"),
                @Index(name = "idx_orders_store_id", columnList = "store_id"),
                @Index(name = "idx_orders_status", columnList = "status"),
                @Index(name = "idx_orders_created_at", columnList = "created_at")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity {

    @Column(name = "order_code", unique = true, nullable = false, length = 50)
    private String orderCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // ===== QUAN HỆ =====

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")   // Nullable: khách vãng lai
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id", nullable = false)
    private User cashier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    // ===== HELPER METHODS =====

    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    // ===== BUSINESS METHODS =====

    public void calculateTotals() {
        this.subtotal = orderItems.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // total = subtotal + tax - discount
        this.totalAmount = this.subtotal
                .add(this.taxAmount)
                .subtract(this.discountAmount);
    }

    public boolean canBeCancelled() {
        return this.status == OrderStatus.PENDING
                || this.status == OrderStatus.CONFIRMED;
    }
}