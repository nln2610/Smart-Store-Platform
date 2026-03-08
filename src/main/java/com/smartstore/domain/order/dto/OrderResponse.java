// OrderResponse.java
package com.smartstore.domain.order.dto;

import com.smartstore.common.enums.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {
    private UUID id;
    private String orderCode;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String notes;
    private UUID storeId;
    private String storeName;
    private UUID customerId;
    private String customerName;
    private UUID cashierId;
    private String cashierName;
    private List<OrderItemResponse> items;
    private Instant createdAt;

    @Data
    @Builder
    public static class OrderItemResponse {
        private UUID id;
        private UUID productId;
        private String productName;
        private String productSku;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}