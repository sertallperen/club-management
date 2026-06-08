package com.clubmanagement.controller;

import com.clubmanagement.dto.common.ApiResponse;
import com.clubmanagement.dto.common.PagedResponse;
import com.clubmanagement.dto.leave.LeaveRequestDto;
import com.clubmanagement.dto.leave.LeaveRequestResponse;
import com.clubmanagement.dto.leave.LeaveReviewRequest;
import com.clubmanagement.service.LeaveRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leave-requests")
@RequiredArgsConstructor
@Tag(name = "Leave Requests", description = "Leave request submission and review")
public class LeaveRequestController {

    private final LeaveRequestService leaveService;

    @GetMapping
    @Operation(summary = "List leave requests (PLAYER sees own, others see all)")
    public ResponseEntity<ApiResponse<PagedResponse<LeaveRequestResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                leaveService.getAll(PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get leave request by ID")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(leaveService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Submit a leave request")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> create(
            @Valid @RequestBody LeaveRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave request submitted", leaveService.create(request)));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICAL_DIRECTOR')")
    @Operation(summary = "Approve or reject a leave request")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> review(
            @PathVariable Long id, @Valid @RequestBody LeaveReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Review recorded", leaveService.review(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete leave request")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        leaveService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.message("Leave request deleted"));
    }
}
