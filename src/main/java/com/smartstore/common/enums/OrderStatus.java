// OrderStatus.java
package com.smartstore.common.enums;

public enum OrderStatus {
    PENDING,    // Chờ xác nhận
    CONFIRMED,  // Đã xác nhận
    COMPLETED,  // Hoàn thành
    CANCELLED,  // Đã hủy
    REFUNDED    // Đã hoàn tiền
}