package com.clubmanagement.repository;

import com.clubmanagement.entity.InjuryReport;
import com.clubmanagement.entity.Player;
import com.clubmanagement.enums.InjuryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InjuryReportRepository extends JpaRepository<InjuryReport, Long> {

    Optional<InjuryReport> findByIdAndDeletedFalse(Long id);

    Page<InjuryReport> findAllByDeletedFalse(Pageable pageable);

    List<InjuryReport> findByPlayerAndDeletedFalseOrderByInjuryDateDesc(Player player);

    List<InjuryReport> findByStatusAndDeletedFalse(InjuryStatus status);
}
