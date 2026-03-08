// OrderCodeGenerator.java
package com.smartstore.common.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OrderCodeGenerator {

    // ⭐ AtomicInteger đảm bảo thread-safe khi nhiều request đồng thời
    private final AtomicInteger counter = new AtomicInteger(0);
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generate() {
        String date = LocalDate.now().format(formatter);
        int seq = counter.incrementAndGet() % 10000; // reset sau 9999
        return String.format("ORD-%s-%04d", date, seq);
        // Kết quả: ORD-20240315-0001
    }
}