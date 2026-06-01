package com.example.digital_donation_api.repository;

import com.example.digital_donation_api.entity.Event;
import com.example.digital_donation_api.entity.EventStatus;
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
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatus(EventStatus status);

    @EntityGraph(attributePaths = {"owner", "charity"})
    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"owner", "charity"})
    @org.springframework.data.jpa.repository.Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Event> searchEvents(@org.springframework.data.repository.query.Param("query") String query, Pageable pageable);

    Long countByStatus(EventStatus status);

    @EntityGraph(attributePaths = {"owner", "charity"})
    Page<Event> findByOwnerId(Long ownerId, Pageable pageable);

    List<Event> findByCharityId(Long charityId);

    Optional<Event> findByJoinCode(String joinCode);

    @Query("""
        SELECT e FROM Event e
        WHERE e.visibility = 'PUBLIC'
          AND e.status IN ('ACTIVE', 'APPROVED')
          AND e.status != 'BLOCKED'
    """)
    @EntityGraph(attributePaths = {"owner", "charity"})
    Page<Event> findPublicActiveEvents(Pageable pageable);

    @Query(value = """
        SELECT e.title, 
               e.goal_amount, 
               COALESCE(SUM(CASE WHEN d.status = 'SUCCESS' THEN d.amount ELSE 0 END), 0) as raised,
               COUNT(d.id) as donations_count
        FROM events e
        LEFT JOIN donations d ON e.id = d.event_id
        WHERE e.owner_id = :userId
        GROUP BY e.id, e.title, e.goal_amount
        ORDER BY raised DESC
    """, nativeQuery = true)
    List<Object[]> findEventsPerformanceByUser(@Param("userId") Long userId);
}
