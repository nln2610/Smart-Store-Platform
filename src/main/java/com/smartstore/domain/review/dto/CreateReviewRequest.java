// CreateReviewRequest.java
package com.smartstore.domain.review.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateReviewRequest {

    @NotNull
    private UUID productId;

    @NotNull
    @Min(value = 1, message = "Rating minimum is 1")
    @Max(value = 5, message = "Rating maximum is 5")
    private Integer rating;

    @Size(max = 1000, message = "Comment too long")
    private String comment;
}