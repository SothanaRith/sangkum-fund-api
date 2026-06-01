package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.Charity;
import com.example.digital_donation_api.entity.CharityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CharityRepository extends JpaRepository<Charity, Long> {

    Optional<Charity> findByOwnerId(Long userId);

    @EntityGraph(attributePaths = {"owner"})
    Page<Charity> findByStatus(CharityStatus status, Pageable pageable);
    
    // Search by name or description
    @Query("SELECT c FROM Charity c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    @EntityGraph(attributePaths = {"owner"})
    Page<Charity> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // Get charities by category
    @EntityGraph(attributePaths = {"owner"})
    Page<Charity> findByCategory(String category, Pageable pageable);
    
    // Get verified charities by category
    @Query("SELECT c FROM Charity c WHERE c.status = 'VERIFIED' AND c.category = :category")
    @EntityGraph(attributePaths = {"owner"})
    Page<Charity> findVerifiedByCategory(@Param("category") String category, Pageable pageable);
}
