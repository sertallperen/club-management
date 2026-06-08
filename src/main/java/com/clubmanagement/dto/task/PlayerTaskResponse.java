package com.clubmanagement.dto.task;

import com.clubmanagement.enums.TaskStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class PlayerTaskResponse {
    private Long id;
    private Long playerId;
    private String playerFullName;
    private String assignedByUsername;
    private String title;
    private String description;
    private LocalDate dueDate;
    private TaskStatus status;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
