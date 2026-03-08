// CreateOrderRequest.java
package com.smartstore.domain.order.dto;

import com.smartstore.common.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Store ID is required")
    private UUID storeId;

    private UUID customerId;        // Optional: null nếu khách vãng lai

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @DecimalMin(value = "0.0")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private String notes;

    @NotEmpty(message = "Order must have at least 1 item")
    @Valid  // ⭐ Validate từng item trong list
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {

        @NotNull(message = "Product ID is required")
        private UUID productId;

        @NotNull
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }
}