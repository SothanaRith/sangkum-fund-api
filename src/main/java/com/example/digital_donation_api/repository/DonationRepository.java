package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.Donation;
import com.example.digital_donation_api.entity.DonationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    List<Donation> findByEventId(Long eventId);

    boolean existsByTransactionRef(String transactionRef);

    java.util.Optional<Donation> findByTransactionRef(String transactionRef);

    long countByEventIdAndStatus(Long eventId, DonationStatus status);

    List<Donation> findTop5ByEventIdOrderByCreatedAtDesc(Long eventId);

    List<Donation> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"event", "user"})
    Page<Donation> findByUserId(Long userId, Pageable pageable);
    
    @EntityGraph(attributePaths = {"event", "user"})
    Page<Donation> findByStatus(DonationStatus status, Pageable pageable);
    
    long countByStatus(DonationStatus status);
    
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Donation d WHERE d.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") DonationStatus status);
    
    @Query("""
        SELECT d FROM Donation d 
        WHERE d.status = :status 
        AND (LOWER(d.user.name) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(d.event.title) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(d.transactionRef) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY d.createdAt DESC
    """)
    @EntityGraph(attributePaths = {"event", "user"})
    Page<Donation> findByStatusAndSearch(
            @Param("status") DonationStatus status,
            @Param("search") String search,
            Pageable pageable
    );
    
    @Query("""
        SELECT d FROM Donation d 
        WHERE LOWER(d.user.name) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(d.event.title) LIKE LOWER(CONCAT('%', :search, '%'))
        OR LOWER(d.transactionRef) LIKE LOWER(CONCAT('%', :search, '%'))
        ORDER BY d.createdAt DESC
    """)
    @EntityGraph(attributePaths = {"event", "user"})
    Page<Donation> findBySearch(@Param("search") String search, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(d.amount), 0)
        FROM Donation d
        WHERE d.event.id = :eventId
          AND d.status = 'SUCCESS'
    """)
    BigDecimal sumSuccessfulDonations(@Param("eventId") Long eventId);

    @Query("""
        SELECT COUNT(DISTINCT d.user.id)
        FROM Donation d
        WHERE d.event.id = :eventId
          AND d.status = 'SUCCESS'
    """)
    Long countDonors(@Param("eventId") Long eventId);

    // Analytics queries
    @Query(value = """
        SELECT DATE_FORMAT(d.created_at, '%Y-%m') as month, 
               COUNT(*) as count, 
               SUM(d.amount) as total
        FROM donations d
        WHERE d.user_id = :userId 
          AND d.created_at >= DATE_SUB(NOW(), INTERVAL :months MONTH)
        GROUP BY DATE_FORMAT(d.created_at, '%Y-%m')
        ORDER BY month DESC
    """, nativeQuery = true)
    List<Object[]> findDonationsByMonth(@Param("userId") Long userId, @Param("months") int months);

    @Query("""
        SELECT d.status, COUNT(d)
        FROM Donation d
        WHERE d.user.id = :userId
        GROUP BY d.status
    """)
    List<Object[]> countByStatusAndUserId(@Param("userId") Long userId);

    @Query("""
      SELECT e.title, SUM(d.amount), COUNT(d)
      FROM Donation d
      JOIN d.event e
      WHERE d.user.id = :userId AND d.status = 'SUCCESS'
      GROUP BY e.id, e.title
      ORDER BY SUM(d.amount) DESC
    """)
    List<Object[]> findTopEventsByUser(@Param("userId") Long userId);

    @Query(value = """
        SELECT DATE(d.created_at) as date, 
               COUNT(*) as count, 
               SUM(d.amount) as total
        FROM donations d
        WHERE d.user_id = :userId 
          AND d.created_at >= DATE_SUB(NOW(), INTERVAL :days DAY)
        GROUP BY DATE(d.created_at)
        ORDER BY date ASC
    """, nativeQuery = true)
    List<Object[]> findDonationTrends(@Param("userId") Long userId, @Param("days") int days);

    @Query("""
        SELECT COALESCE(SUM(d.amount), 0)
        FROM Donation d
        WHERE d.event.id = :eventId
          AND d.user.id = :userId
          AND d.status = 'SUCCESS'
    """)
    BigDecimal sumSuccessfulDonationsByEventAndUser(@Param("eventId") Long eventId, @Param("userId") Long userId);

    @Query("""
        SELECT COUNT(d)
        FROM Donation d
        WHERE d.event.id = :eventId
          AND d.user.id = :userId
          AND d.status = 'SUCCESS'
    """)
    Long countSuccessfulDonationsByEventAndUser(@Param("eventId") Long eventId, @Param("userId") Long userId);
}
