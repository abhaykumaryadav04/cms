package com.a4b.cms.club.repo;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.a4b.cms.club.entity.Club;
import com.a4b.cms.club.entity.ClubStatus;



public interface ClubRepo  extends JpaRepository<Club,UUID>{
    Optional<Club> findBySlugIgnoreCase(String slug);
    boolean existsBySlugIgnoreCase(String slug); 
    
    @Query("""
        SELECT c
        FROM Club c
        WHERE c.status = :status AND (:search IS NULL
              OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(COALESCE(c.description, ''))
                    LIKE LOWER(CONCAT('%', :search, '%'))
          )
          AND (:category IS NULL
              OR LOWER(c.category) = LOWER(:category)
          )
        """)
    Page<Club> searchClubs(
            @Param("status") ClubStatus status,
            @Param("search") String search,
            @Param("category") String category,
            Pageable pageable
    );

}
