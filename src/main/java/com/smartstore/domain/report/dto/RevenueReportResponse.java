// RevenueReportResponse.java
package com.smartstore.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportResponse {
    private String period;          // "2024-03-01", "2024-W10", "2024-03"
    private BigDecimal revenue;
    private Long orderCount;
    private BigDecimal averageOrderValue;
}