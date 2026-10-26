package com.a4b.cms.event.repo;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.a4b.cms.event.entity.Event;
import com.a4b.cms.event.entity.EventStatus;

import jakarta.persistence.LockModeType;

public interface EventRepo  extends  JpaRepository<Event,UUID>{
     Optional<Event> findByIdAndStatus(UUID id,EventStatus status );
       Page<Event> findByClub_Id(UUID clubId,Pageable pageable);
         @Query(
        value = """
            SELECT e FROM Event e
            WHERE e.status = :status
              AND (:clubId IS NULL OR e.club.id = :clubId)
              AND ( :startsAfter IS NULL OR e.startsAt >= :startsAfter)
              AND (:startsBefore IS NULL OR e.startsAt <= :startsBefore)
              AND (:search IS NULL OR LOWER(e.title)
            LIKE LOWER(CONCAT('%', :search, '%'))  OR LOWER(COALESCE(e.description, ''))
             LIKE LOWER(CONCAT('%', :search, '%')))
            """,
        countQuery = """
            SELECT COUNT(e) FROM Event e
            WHERE e.status = :status
              AND (:clubId IS NULLOR e.club.id = :clubId)
              AND (:startsAfter IS NULLOR e.startsAt >= :startsAfter)
              AND ( :startsBefore IS NULL OR e.startsAt <= :startsBefore)
              AND (:search IS NULL OR LOWER(e.title)
                       LIKE LOWER(CONCAT('%', :search, '%'))
                  OR LOWER(COALESCE(e.description, ''))
                       LIKE LOWER(CONCAT('%', :search, '%')) )
            """
    )
    Page<Event> searchApprovedEvents(
            @Param("status") EventStatus status,
            @Param("clubId") UUID clubId,
            @Param("startsAfter") Instant startsAfter,
            @Param("startsBefore") Instant startsBefore,
            @Param("search") String search,
            Pageable pageable
    );
      @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT e FROM Event e WHERE e.id = :eventId
        """)
    Optional<Event> findByIdForUpdate( @Param("eventId") UUID eventId);


}
