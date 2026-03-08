package com.smartstore.security;

import com.smartstore.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
// ⭐ @RequiredArgsConstructor: Lombok tự tạo constructor inject
//    tất cả field `final` — thay thế cho @Autowired
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // ⭐ Spring Security gọi method này mỗi khi cần verify user
        return userRepository.findByEmailWithRoles(email)
                .map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}