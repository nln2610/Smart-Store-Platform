// OrderController.java
package com.smartstore.domain.order.controller;

import com.smartstore.common.response.ApiResponse;
import com.smartstore.domain.order.dto.*;
import com.smartstore.domain.order.service.OrderService;
import com.smartstore.domain.user.entity.User;
import com.smartstore.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER', 'CASHIER')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User cashier = principal.getUser();
        OrderResponse response = orderService.createOrder(request, cashier);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<OrderResponse>success("Order created successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER', 'CASHIER')")
    public ResponseEntity<ApiResponse<OrderResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>success(orderService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrders(
            @RequestParam UUID storeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(
                ApiResponse.<Page<OrderResponse>>success(
                        orderService.getOrdersByStore(storeId, status, pageable)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>success(
                        "Status updated", orderService.updateStatus(id, request)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER', 'CASHIER')")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>success(
                        "Order cancelled", orderService.cancelOrder(id, principal.getUser())));
    }
}