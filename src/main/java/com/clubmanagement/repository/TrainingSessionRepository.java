package com.clubmanagement.repository;

import com.clubmanagement.entity.TrainingSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {

    Optional<TrainingSession> findByIdAndDeletedFalse(Long id);

    Page<TrainingSession> findAllByDeletedFalse(Pageable pageable);

    List<TrainingSession> findBySessionDateAndDeletedFalseOrderByStartTimeAsc(LocalDate date);

    List<TrainingSession> findBySessionDateBetweenAndDeletedFalseOrderBySessionDateAsc(
            LocalDate from, LocalDate to);
}
