// OrderServiceImpl.java
package com.smartstore.domain.order.service;

import com.smartstore.common.enums.*;
import com.smartstore.common.exception.BusinessException;
import com.smartstore.common.exception.ResourceNotFoundException;
import com.smartstore.common.util.OrderCodeGenerator;
import com.smartstore.domain.inventory.entity.InventoryTransaction;
import com.smartstore.domain.inventory.repository.InventoryTransactionRepository;
import com.smartstore.domain.order.dto.*;
import com.smartstore.domain.order.entity.Order;
import com.smartstore.domain.order.entity.OrderItem;
import com.smartstore.domain.order.repository.OrderRepository;
import com.smartstore.domain.product.entity.Product;
import com.smartstore.domain.product.repository.ProductRepository;
import com.smartstore.domain.store.entity.Store;
import com.smartstore.domain.store.repository.StoreRepository;
import com.smartstore.domain.user.entity.User;
import com.smartstore.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final OrderCodeGenerator orderCodeGenerator;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, User cashier) {
        // 1. Validate store
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        // 2. Validate customer (nếu có)
        User customer = null;
        if (request.getCustomerId() != null) {
            customer = userRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        }

        // 3. Tạo Order trước (chưa có items)
        Order order = Order.builder()
                .orderCode(orderCodeGenerator.generate())
                .store(store)
                .cashier(cashier)
                .customer(customer)
                .paymentMethod(request.getPaymentMethod())
                .discountAmount(request.getDiscountAmount())
                .notes(request.getNotes())
                .status(OrderStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.PAID)
                .build();

        // 4. Xử lý từng item — trừ kho và tạo OrderItem
        List<InventoryTransaction> inventoryLogs = new ArrayList<>();

        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findByIdAndIsActiveTrue(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found: " + itemReq.getProductId()));

            int stockBefore = product.getStockQuantity();

            // ⭐ decreaseStock() tự throw InsufficientStockException nếu không đủ hàng
            product.decreaseStock(itemReq.getQuantity());
            productRepository.save(product);

            // Tạo OrderItem với giá TẠI THỜI ĐIỂM mua
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(product.getPrice()
                            .multiply(java.math.BigDecimal.valueOf(itemReq.getQuantity())))
                    .build();

            order.addOrderItem(orderItem);

            // Ghi log xuất kho
            inventoryLogs.add(InventoryTransaction.builder()
                    .product(product)
                    .type(TransactionType.EXPORT)
                    .quantity(itemReq.getQuantity())
                    .stockBefore(stockBefore)
                    .stockAfter(product.getStockQuantity())
                    .note("Sold via order: " + order.getOrderCode())
                    .performedBy(cashier)
                    .referenceId(order.getId())
                    .build());
        }

        // 5. Tính tổng tiền
        order.calculateTotals();

        // 6. Lưu order (cascade sẽ tự lưu OrderItems)
        Order saved = orderRepository.save(order);

        // 7. Lưu inventory logs
        inventoryTransactionRepository.saveAll(inventoryLogs);

        log.info("✅ Order created: {} - Total: {}", saved.getOrderCode(), saved.getTotalAmount());

        return toResponse(saved);
    }

    @Override
    public OrderResponse getById(UUID id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        return toResponse(order);
    }

    @Override
    public Page<OrderResponse> getOrdersByStore(UUID storeId, String status, Pageable pageable) {
        OrderStatus orderStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                orderStatus = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid status: " + status);
            }
        }
        return orderRepository
                .findByStoreWithFilters(storeId, orderStatus, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(UUID id, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        // Validate chuyển trạng thái hợp lệ
        validateStatusTransition(order.getStatus(), request.getStatus());

        order.setStatus(request.getStatus());

        // Nếu hoàn tiền → cập nhật payment status
        if (request.getStatus() == OrderStatus.REFUNDED) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
            restoreStock(order);  // Hoàn lại tồn kho
        }

        return toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID id, User currentUser) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        if (!order.canBeCancelled()) {
            throw new BusinessException(
                    "Cannot cancel order with status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        restoreStock(order);  // Hoàn lại tồn kho khi hủy

        log.info("Order cancelled: {}", order.getOrderCode());
        return toResponse(orderRepository.save(order));
    }

    // ===== PRIVATE HELPERS =====

    // ⭐ Hoàn lại tồn kho khi hủy đơn hoặc hoàn tiền
    private void restoreStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            int stockBefore = product.getStockQuantity();
            product.increaseStock(item.getQuantity());
            productRepository.save(product);

            inventoryTransactionRepository.save(
                    InventoryTransaction.builder()
                            .product(product)
                            .type(TransactionType.RETURN)
                            .quantity(item.getQuantity())
                            .stockBefore(stockBefore)
                            .stockAfter(product.getStockQuantity())
                            .note("Stock restored for order: " + order.getOrderCode())
                            .referenceId(order.getId())
                            .build()
            );
        }
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        // ⭐ Định nghĩa luồng trạng thái hợp lệ
        boolean valid = switch (current) {
            case PENDING   -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.COMPLETED || next == OrderStatus.CANCELLED;
            case COMPLETED -> next == OrderStatus.REFUNDED;
            default        -> false; // CANCELLED, REFUNDED không chuyển tiếp được
        };

        if (!valid) {
            throw new BusinessException(
                    "Invalid status transition: " + current + " → " + next);
        }
    }

    // ⭐ Mapper tập trung 1 chỗ
    private OrderResponse toResponse(Order o) {
        List<OrderResponse.OrderItemResponse> itemResponses = o.getOrderItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productSku(item.getProduct().getSku())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(o.getId())
                .orderCode(o.getOrderCode())
                .status(o.getStatus())
                .subtotal(o.getSubtotal())
                .discountAmount(o.getDiscountAmount())
                .taxAmount(o.getTaxAmount())
                .totalAmount(o.getTotalAmount())
                .paymentMethod(o.getPaymentMethod())
                .paymentStatus(o.getPaymentStatus())
                .notes(o.getNotes())
                .storeId(o.getStore().getId())
                .storeName(o.getStore().getName())
                .customerId(o.getCustomer() != null ? o.getCustomer().getId() : null)
                .customerName(o.getCustomer() != null ? o.getCustomer().getFullName() : null)
                .cashierId(o.getCashier().getId())
                .cashierName(o.getCashier().getFullName())
                .items(itemResponses)
                .createdAt(o.getCreatedAt())
                .build();
    }
}