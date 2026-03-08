package com.smartstore.domain.inventory.dto;

import com.smartstore.common.enums.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class InventoryTransactionResponse {
    private UUID id;
    private UUID productId;
    private String productName;
    private String productSku;
    private TransactionType type;
    private Integer quantity;
    private Integer stockBefore;
    private Integer stockAfter;
    private String note;
    private Instant createdAt;
}