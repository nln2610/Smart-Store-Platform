// OrderService.java (interface)
package com.smartstore.domain.order.service;

import com.smartstore.domain.order.dto.*;
import com.smartstore.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request, User cashier);
    OrderResponse getById(UUID id);
    Page<OrderResponse> getOrdersByStore(UUID storeId, String status, Pageable pageable);
    OrderResponse updateStatus(UUID id, UpdateOrderStatusRequest request);
    OrderResponse cancelOrder(UUID id, User currentUser);
}