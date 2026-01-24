package com.example.digital_donation_api.service.impl;

import com.example.digital_donation_api.entity.Role;
import com.example.digital_donation_api.entity.User;
import com.example.digital_donation_api.entity.UserRole;
import com.example.digital_donation_api.repository.RoleRepository;
import com.example.digital_donation_api.repository.UserRepository;
import com.example.digital_donation_api.repository.UserRoleRepository;
import com.example.digital_donation_api.service.AuthService;
import com.example.digital_donation_api.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public String login(String email, String password) {
        try {
            log.debug("Login attempt for email: {}", email);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));

            log.debug("User found: {}, checking password", user.getEmail());
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("Invalid credentials");
            }

            log.debug("Password matches, generating token");
            String token = jwtService.generateToken(user);
            log.debug("Token generated successfully");
            return token;
        } catch (Exception e) {
            log.error("Login error for email: {}", email, e);
            throw e;
        }
    }

    @Override
    public User register(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        user.setIsActive(true);
        userRepository.save(user);

        Role donorRole = roleRepository.findByName("DONOR")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("DONOR");
                    return roleRepository.save(role);
                });

        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(donorRole);
        userRoleRepository.save(userRole);

        return user;
    }
}
