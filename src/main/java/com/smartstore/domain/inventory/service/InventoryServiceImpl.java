// InventoryServiceImpl.java
package com.smartstore.domain.inventory.service;

import com.smartstore.common.enums.TransactionType;
import com.smartstore.common.exception.ResourceNotFoundException;
import com.smartstore.domain.inventory.dto.AdjustStockRequest;
import com.smartstore.domain.inventory.dto.InventoryTransactionResponse;
import com.smartstore.domain.inventory.entity.InventoryTransaction;
import com.smartstore.domain.inventory.repository.InventoryTransactionRepository;
import com.smartstore.domain.product.entity.Product;
import com.smartstore.domain.product.repository.ProductRepository;
import com.smartstore.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl {

    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;

    @Transactional
    public InventoryTransactionResponse adjustStock(AdjustStockRequest request, User performedBy) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        int stockBefore = product.getStockQuantity();

        // ⭐ Dựa vào type để tăng hoặc giảm kho
        if (request.getType() == TransactionType.IMPORT
                || request.getType() == TransactionType.RETURN) {
            product.increaseStock(request.getQuantity());
        } else {
            product.decreaseStock(request.getQuantity());  // Tự throw nếu không đủ hàng
        }

        productRepository.save(product);

        // Ghi lại lịch sử giao dịch kho
        InventoryTransaction transaction = InventoryTransaction.builder()
                .product(product)
                .type(request.getType())
                .quantity(request.getQuantity())
                .stockBefore(stockBefore)
                .stockAfter(product.getStockQuantity())
                .note(request.getNote())
                .performedBy(performedBy)
                .build();

        InventoryTransaction saved = transactionRepository.save(transaction);
        // ⭐ Map sang DTO trước khi trả về — không trả Entity trực tiếp
        return InventoryTransactionResponse.builder()
                .id(saved.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productSku(product.getSku())
                .type(saved.getType())
                .quantity(saved.getQuantity())
                .stockBefore(saved.getStockBefore())
                .stockAfter(saved.getStockAfter())
                .note(saved.getNote())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}