package com.clubmanagement.repository;

import com.clubmanagement.entity.Player;
import com.clubmanagement.entity.PlayerTask;
import com.clubmanagement.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerTaskRepository extends JpaRepository<PlayerTask, Long> {

    Optional<PlayerTask> findByIdAndDeletedFalse(Long id);

    Page<PlayerTask> findAllByDeletedFalse(Pageable pageable);

    List<PlayerTask> findByPlayerAndDeletedFalseOrderByCreatedAtDesc(Player player);

    List<PlayerTask> findByPlayerAndStatusAndDeletedFalse(Player player, TaskStatus status);
}
