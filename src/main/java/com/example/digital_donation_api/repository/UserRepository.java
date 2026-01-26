package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // User search and filtering methods
    @Query("SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> findBySearch(@Param("search") String search, Pageable pageable);

    // Active/Inactive users
    Page<User> findByIsActive(boolean isActive, Pageable pageable);

    long countByIsActive(boolean isActive);

    // Blocked users
    Page<User> findByIsBlocked(boolean isBlocked, Pageable pageable);

    long countByIsBlocked(boolean isBlocked);
}
