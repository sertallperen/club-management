package com.clubmanagement.service;

import com.clubmanagement.dto.common.PagedResponse;
import com.clubmanagement.dto.task.PlayerTaskRequest;
import com.clubmanagement.dto.task.PlayerTaskResponse;
import com.clubmanagement.entity.Player;
import com.clubmanagement.entity.PlayerTask;
import com.clubmanagement.entity.User;
import com.clubmanagement.enums.RoleName;
import com.clubmanagement.enums.TaskStatus;
import com.clubmanagement.exception.BusinessRuleException;
import com.clubmanagement.exception.ResourceNotFoundException;
import com.clubmanagement.repository.PlayerRepository;
import com.clubmanagement.repository.PlayerTaskRepository;
import com.clubmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerTaskService {

    private final PlayerTaskRepository taskRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;

    public PagedResponse<PlayerTaskResponse> getAll(Pageable pageable) {
        User current = getCurrentUser();
        if (current.getRole() == RoleName.PLAYER) {
            Player player = playerRepository.findByUserAndDeletedFalse(current)
                    .orElseThrow(() -> new BusinessRuleException("Player profile not found"));
            List<PlayerTaskResponse> tasks = taskRepository
                    .findByPlayerAndDeletedFalseOrderByCreatedAtDesc(player)
                    .stream().map(this::toResponse).toList();
            // wrap in paged response manually for consistency
            return PagedResponse.<PlayerTaskResponse>builder()
                    .content(tasks).page(0).size(tasks.size())
                    .totalElements(tasks.size()).totalPages(1).last(true).build();
        }
        return PagedResponse.of(taskRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    public PlayerTaskResponse getById(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public PlayerTaskResponse create(PlayerTaskRequest request) {
        Player player = playerRepository.findByIdAndDeletedFalse(request.getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player", request.getPlayerId()));
        User assignedBy = getCurrentUser();

        PlayerTask task = PlayerTask.builder()
                .player(player)
                .assignedBy(assignedBy)
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .status(TaskStatus.PENDING)
                .build();

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public PlayerTaskResponse updateStatus(Long id, TaskStatus newStatus) {
        PlayerTask task = findActive(id);
        task.setStatus(newStatus);
        if (newStatus == TaskStatus.COMPLETED) {
            task.setCompletedAt(LocalDateTime.now());
        }
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void softDelete(Long id) {
        PlayerTask task = findActive(id);
        task.setDeleted(true);
        taskRepository.save(task);
    }

    private PlayerTask findActive(Long id) {
        return taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("PlayerTask", id));
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new BusinessRuleException("Current user not found"));
    }

    public PlayerTaskResponse toResponse(PlayerTask t) {
        Player p = t.getPlayer();
        return PlayerTaskResponse.builder()
                .id(t.getId())
                .playerId(p.getId())
                .playerFullName(p.getFirstName() + " " + p.getLastName())
                .assignedByUsername(t.getAssignedBy().getUsername())
                .title(t.getTitle())
                .description(t.getDescription())
                .dueDate(t.getDueDate())
                .status(t.getStatus())
                .completedAt(t.getCompletedAt())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
