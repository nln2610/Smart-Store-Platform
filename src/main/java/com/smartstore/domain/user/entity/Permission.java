package com.smartstore.domain.user.entity;

import com.smartstore.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;          // Ví dụ: "product:create", "order:read"

    @Column(nullable = false, length = 50)
    private String resource;      // Ví dụ: "product", "order"

    @Column(nullable = false, length = 50)
    private String action;        // Ví dụ: "create", "read", "update", "delete"

    @ManyToMany(mappedBy = "permissions")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}