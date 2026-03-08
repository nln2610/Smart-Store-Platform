package com.smartstore.domain.report.repository;

import com.smartstore.domain.report.dto.RevenueReportResponse;
import com.smartstore.domain.report.dto.TopProductResponse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

// ⭐ Không extends JpaRepository vì đây là custom query repo
//    Dùng EntityManager trực tiếp sẽ linh hoạt hơn
@Repository
public interface ReportRepository {
}