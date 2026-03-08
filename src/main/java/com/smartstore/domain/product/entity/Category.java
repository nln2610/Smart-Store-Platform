package com.smartstore.domain.product.entity;

import com.smartstore.common.entity.BaseEntity;
import com.smartstore.domain.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;              // vd: "do-uong", "thuc-pham"

    // ⭐ Self-join: category có thể có cha (Đồ uống → Nước ngọt → Coca Cola)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;
}