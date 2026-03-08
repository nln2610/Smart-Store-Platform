package com.smartstore.config;

import com.smartstore.common.enums.RoleName;
import com.smartstore.domain.user.entity.Role;
import com.smartstore.domain.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
// ⭐ CommandLineRunner: Spring tự gọi method run() sau khi app khởi động xong
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        seedRoles();
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
}