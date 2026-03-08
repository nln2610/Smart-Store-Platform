package com.smartstore.domain.auth.service;

import com.smartstore.common.enums.RoleName;
import com.smartstore.common.exception.BusinessException;
import com.smartstore.domain.auth.dto.*;
import com.smartstore.domain.user.entity.Role;
import com.smartstore.domain.user.entity.User;
import com.smartstore.domain.user.repository.RoleRepository;
import com.smartstore.domain.user.repository.UserRepository;
import com.smartstore.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional  // ⭐ Đảm bảo register là atomic — lỗi giữa chừng sẽ rollback
    public TokenResponse register(RegisterRequest request) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered: " + request.getEmail());
        }

        // Lấy role CUSTOMER mặc định
        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new IllegalStateException("CUSTOMER role not found. Run data seeder first."));

        // Tạo user mới
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                // ⭐ encode() dùng BCrypt — không bao giờ lưu plain text
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .build();

        user.addRole(customerRole);
        userRepository.save(user);

        return buildTokenResponse(user);
    }

    public TokenResponse login(LoginRequest request) {
        // ⭐ authenticate() sẽ tự throw BadCredentialsException nếu sai
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmailWithRoles(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return buildTokenResponse(user);
    }

    // Tách ra method riêng vì cả register và login đều dùng
    private TokenResponse buildTokenResponse(User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        String token = jwtService.generateToken(
                user.getEmail(),
                Map.of("roles", roleNames, "userId", user.getId().toString())
        );

        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(86400)
                .user(TokenResponse.UserInfo.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .roles(roleNames)
                        .build())
                .build();
    }
}