// TopProductResponse.java
package com.smartstore.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductResponse {
    private UUID productId;
    private String productName;
    private String productSku;
    private Long quantitySold;
    private BigDecimal revenue;
}