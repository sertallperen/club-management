package com.clubmanagement.controller;

import com.clubmanagement.dto.common.ApiResponse;
import com.clubmanagement.dto.common.PagedResponse;
import com.clubmanagement.dto.task.PlayerTaskRequest;
import com.clubmanagement.dto.task.PlayerTaskResponse;
import com.clubmanagement.enums.TaskStatus;
import com.clubmanagement.service.PlayerTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Player Tasks", description = "Task assignment and tracking")
public class PlayerTaskController {

    private final PlayerTaskService taskService;

    @GetMapping
    @Operation(summary = "List tasks (PLAYER sees own, others see all)")
    public ResponseEntity<ApiResponse<PagedResponse<PlayerTaskResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                taskService.getAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<ApiResponse<PlayerTaskResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(taskService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICAL_DIRECTOR', 'ASSISTANT_COACH')")
    @Operation(summary = "Assign a task to a player")
    public ResponseEntity<ApiResponse<PlayerTaskResponse>> create(@Valid @RequestBody PlayerTaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task assigned", taskService.create(request)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status")
    public ResponseEntity<ApiResponse<PlayerTaskResponse>> updateStatus(
            @PathVariable Long id, @RequestParam TaskStatus status) {
        return ResponseEntity.ok(ApiResponse.success(taskService.updateStatus(id, status)));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('PLAYER')")
    @Operation(summary = "Mark task as completed (PLAYER only)")
    public ResponseEntity<ApiResponse<PlayerTaskResponse>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(taskService.updateStatus(id, TaskStatus.COMPLETED)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete task")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        taskService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.message("Task deleted"));
    }
}
