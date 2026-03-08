package com.smartstore.config;

import com.smartstore.common.enums.RoleName;
import com.smartstore.domain.store.entity.Store;
import com.smartstore.domain.store.repository.StoreRepository;
import com.smartstore.domain.user.entity.Role;
import com.smartstore.domain.user.entity.User;
import com.smartstore.domain.user.repository.RoleRepository;
import com.smartstore.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
// ⭐ CommandLineRunner: Spring tự gọi method run() sau khi app khởi động xong
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedRoles();
        seedDefaultStore();
        seedAdminUser();
    }

    private void seedRoles() {
        // Load tất cả roles hiện có chỉ với 1 query duy nhất
        var existingRoles = roleRepository.findAll()
                .stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toSet());

        Arrays.stream(RoleName.values())
                .filter(roleName -> !existingRoles.contains(roleName))
                .forEach(roleName -> {
                    Role role = Role.builder()
                            .name(roleName)
                            .description("Default role for " + roleName.name())
                            .build();
                    roleRepository.save(role);
                    log.info("✅ Created role: {}", roleName);
                });
    }

    private void seedDefaultStore() {
        if (storeRepository.count() == 0) {
            Store store = Store.builder()
                    .name("Smart Store - Chi nhánh 1")
                    .address("123 Nguyễn Văn A, Q.1, TP.HCM")
                    .phone("0901234567")
                    .build();
            storeRepository.save(store);
            log.info("✅ Created default store");
        }
    }

    private void seedAdminUser() {
        if (userRepository.existsByEmail("admin@smartstore.com")) return;

        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow();

        User admin = User.builder()
                .email("admin@smartstore.com")
                .passwordHash(passwordEncoder.encode("Admin@123"))
                .fullName("System Admin")
                .build();

        admin.addRole(adminRole);
        userRepository.save(admin);
        log.info("✅ Created admin user: admin@smartstore.com / Admin@123");
    }
}