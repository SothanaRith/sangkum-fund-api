package com.example.digital_donation_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    private String avatar;
    private String phone;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @JsonIgnore
    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    @JsonIgnore
    @Column(name = "account_locked")
    private Boolean accountLocked = false;

    @JsonIgnore
    @Column(name = "lock_time")
    private LocalDateTime lockTime;
    
    @Column(name = "is_blocked")
    private Boolean isBlocked = false;
    
    @Column(name = "block_reason")
    private String blockReason;
    
    @Column(name = "blocked_at")
    private LocalDateTime blockedAt;
    
    @Column(name = "blocked_by")
    private Long blockedBy;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "owner")
    @JsonIgnore
    private List<Event> ownedEvents = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<UserRole> userRoles = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userRoles.stream()
                .map(userRole -> (GrantedAuthority) () -> "ROLE_" + userRole.getRole().getName())
                .toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // Check if account is locked
        if (accountLocked != null && accountLocked) {
            // If lock time is set, check if lock period has expired (e.g., 30 minutes)
            if (lockTime != null) {
                LocalDateTime unlockTime = lockTime.plusMinutes(30);
                if (LocalDateTime.now().isAfter(unlockTime)) {
                    // Lock period expired, account should be unlocked
                    return true;
                }
                return false;
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
