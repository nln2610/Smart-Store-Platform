// OrderCodeGenerator.java
package com.smartstore.common.util;

import com.smartstore.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class OrderCodeGenerator {

    private final OrderRepository orderRepository;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generate() {
        String date = LocalDate.now().format(formatter);
        String prefix = "ORD-" + date + "-";

        // Đọc từ DB → không bị reset khi app restart
        long count = orderRepository.countByOrderCodeStartingWith(prefix);
        long seq = count + 1;

        return String.format("ORD-%s-%04d", date, seq);
    }
}