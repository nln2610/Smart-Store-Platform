package com.smartstore.domain.inventory.controller;

import com.smartstore.common.response.ApiResponse;
import com.smartstore.domain.inventory.dto.AdjustStockRequest;
import com.smartstore.domain.inventory.dto.InventoryTransactionResponse;
import com.smartstore.domain.inventory.service.InventoryServiceImpl;
import com.smartstore.domain.user.entity.User;
import com.smartstore.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryServiceImpl inventoryService;

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<InventoryTransactionResponse>> adjustStock(
            @Valid @RequestBody AdjustStockRequest request,
            @AuthenticationPrincipal UserPrincipal principal
            // ⭐ @AuthenticationPrincipal lấy user đang đăng nhập từ SecurityContext
            //    Không cần query DB thêm lần nào
    ) {
        User currentUser = principal.getUser();
        InventoryTransactionResponse result = inventoryService.adjustStock(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Stock adjusted successfully", result));
    }
}