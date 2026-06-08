package com.clubmanagement.controller;

import com.clubmanagement.dto.common.ApiResponse;
import com.clubmanagement.dto.common.PagedResponse;
import com.clubmanagement.dto.injury.InjuryReportRequest;
import com.clubmanagement.dto.injury.InjuryReportResponse;
import com.clubmanagement.enums.InjuryStatus;
import com.clubmanagement.service.InjuryReportService;
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
@RequestMapping("/api/injuries")
@RequiredArgsConstructor
@Tag(name = "Injury Reports", description = "Player injury tracking")
public class InjuryReportController {

    private final InjuryReportService injuryService;

    @GetMapping
    @Operation(summary = "List injury reports (PLAYER sees own only)")
    public ResponseEntity<ApiResponse<PagedResponse<InjuryReportResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                injuryService.getAll(PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get injury report by ID")
    public ResponseEntity<ApiResponse<InjuryReportResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(injuryService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Report an injury")
    public ResponseEntity<ApiResponse<InjuryReportResponse>> create(
            @Valid @RequestBody InjuryReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Injury reported", injuryService.create(request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICAL_DIRECTOR', 'ASSISTANT_COACH')")
    @Operation(summary = "Update injury status")
    public ResponseEntity<ApiResponse<InjuryReportResponse>> updateStatus(
            @PathVariable Long id, @RequestParam InjuryStatus status) {
        return ResponseEntity.ok(ApiResponse.success(injuryService.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete injury report")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        injuryService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.message("Injury report deleted"));
    }
}
