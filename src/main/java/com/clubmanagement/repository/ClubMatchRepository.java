package com.clubmanagement.repository;

import com.clubmanagement.entity.ClubMatch;
import com.clubmanagement.enums.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClubMatchRepository extends JpaRepository<ClubMatch, Long> {

    Optional<ClubMatch> findByIdAndDeletedFalse(Long id);

    Page<ClubMatch> findAllByDeletedFalse(Pageable pageable);

    // Upcoming matches sorted by date
    List<ClubMatch> findByStatusAndDeletedFalseAndMatchDateAfterOrderByMatchDateAsc(
            MatchStatus status, LocalDateTime after);

    // Date range filtering
    @Query("SELECT m FROM ClubMatch m WHERE m.deleted = false " +
           "AND (:status IS NULL OR m.status = :status) " +
           "AND (:from IS NULL OR m.matchDate >= :from) " +
           "AND (:to IS NULL OR m.matchDate <= :to) " +
           "AND (:opponent IS NULL OR LOWER(m.opponent) LIKE LOWER(CONCAT('%', :opponent, '%')))")
    Page<ClubMatch> searchMatches(@Param("status") MatchStatus status,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to,
                                  @Param("opponent") String opponent,
                                  Pageable pageable);
}
