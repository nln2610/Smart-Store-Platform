// UpdateOrderStatusRequest.java
package com.smartstore.domain.order.dto;

import com.smartstore.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;

    private String note;
}