package com.clubmanagement.controller;

import com.clubmanagement.dto.common.ApiResponse;
import com.clubmanagement.dto.common.PagedResponse;
import com.clubmanagement.dto.training.TrainingSessionRequest;
import com.clubmanagement.dto.training.TrainingSessionResponse;
import com.clubmanagement.service.TrainingSessionService;
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

import java.util.List;

@RestController
@RequestMapping("/api/training-sessions")
@RequiredArgsConstructor
@Tag(name = "Training Sessions", description = "Training session management")
public class TrainingSessionController {

    private final TrainingSessionService trainingService;

    @GetMapping
    @Operation(summary = "List all training sessions (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<TrainingSessionResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                trainingService.getAll(PageRequest.of(page, size, Sort.by("sessionDate").descending()))));
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's training sessions")
    public ResponseEntity<ApiResponse<List<TrainingSessionResponse>>> getToday() {
        return ResponseEntity.ok(ApiResponse.success(trainingService.getToday()));
    }

    @GetMapping("/week")
    @Operation(summary = "Get training sessions for the next 7 days")
    public ResponseEntity<ApiResponse<List<TrainingSessionResponse>>> getWeek() {
        return ResponseEntity.ok(ApiResponse.success(trainingService.getWeek()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get training session by ID")
    public ResponseEntity<ApiResponse<TrainingSessionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(trainingService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICAL_DIRECTOR', 'ASSISTANT_COACH')")
    @Operation(summary = "Create training session")
    public ResponseEntity<ApiResponse<TrainingSessionResponse>> create(
            @Valid @RequestBody TrainingSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Training session created", trainingService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICAL_DIRECTOR', 'ASSISTANT_COACH')")
    @Operation(summary = "Update training session")
    public ResponseEntity<ApiResponse<TrainingSessionResponse>> update(
            @PathVariable Long id, @Valid @RequestBody TrainingSessionRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Updated", trainingService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICAL_DIRECTOR')")
    @Operation(summary = "Soft-delete training session")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        trainingService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.message("Training session deleted"));
    }
}
