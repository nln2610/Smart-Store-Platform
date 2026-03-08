package com.smartstore.domain.user.entity;

import com.smartstore.common.entity.BaseEntity;
import com.smartstore.common.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    @Enumerated(EnumType.STRING)
    // ⭐ EnumType.STRING: lưu "ADMIN" thay vì số 0, 1, 2...
    //    Dùng STRING luôn an toàn hơn vì thêm/xóa enum không bị lệch index
    @Column(nullable = false, unique = true, length = 50)
    private RoleName name;

    @Column(length = 200)
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    // ⭐ EAGER ở đây vì Role thường nhỏ và luôn cần permissions khi check auth
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}