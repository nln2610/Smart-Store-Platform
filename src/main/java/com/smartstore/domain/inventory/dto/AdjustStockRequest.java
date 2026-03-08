// AdjustStockRequest.java
package com.smartstore.domain.inventory.dto;

import com.smartstore.common.enums.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class AdjustStockRequest {

    @NotNull
    private UUID productId;

    @NotNull
    private TransactionType type;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;        // luôn dương, service sẽ tự xử lý dương/âm

    private String note;
}