package com.smartstore.domain.store.entity;

import com.smartstore.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stores")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}