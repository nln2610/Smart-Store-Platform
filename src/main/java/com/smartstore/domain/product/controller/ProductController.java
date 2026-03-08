package com.smartstore.domain.product.controller;

import com.smartstore.common.response.ApiResponse;
import com.smartstore.domain.product.dto.*;
import com.smartstore.domain.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
    // ⭐ Chỉ Admin và Manager mới được tạo sản phẩm
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody CreateProductRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created", productService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> search(
            @RequestParam UUID storeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(
                ApiResponse.success(productService.search(storeId, keyword, categoryId, pageable))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.success("Product updated", productService.update(id, request))
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted", null));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_MANAGER')")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getLowStock(
            @RequestParam UUID storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                ApiResponse.success(productService.getLowStockProducts(storeId, pageable))
        );
    }
}