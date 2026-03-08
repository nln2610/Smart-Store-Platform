package com.smartstore.security;

import com.smartstore.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

// ⭐ UserDetails là interface của Spring Security
//    Spring Security dùng object này để check auth, KHÔNG dùng User entity trực tiếp
//    => Tách biệt domain logic và security logic
@Getter
public class UserPrincipal implements UserDetails {

    private final User user;  // Giữ reference để lấy thêm thông tin khi cần

    public UserPrincipal(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // ⭐ Chuyển Role thành GrantedAuthority để Spring Security hiểu
        //    Prefix "ROLE_" là convention của Spring Security
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() { return user.getPasswordHash(); }

    @Override
    public String getUsername() { return user.getEmail(); }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return user.getIsActive(); }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return user.getIsActive(); }
}