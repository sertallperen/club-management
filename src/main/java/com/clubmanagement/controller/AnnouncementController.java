package com.clubmanagement.controller;

import com.clubmanagement.dto.announcement.AnnouncementRequest;
import com.clubmanagement.dto.announcement.AnnouncementResponse;
import com.clubmanagement.dto.common.ApiResponse;
import com.clubmanagement.dto.common.PagedResponse;
import com.clubmanagement.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
@Tag(name = "Announcements", description = "Club announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    @Operation(summary = "Get announcements visible to current user's role")
    public ResponseEntity<ApiResponse<List<AnnouncementResponse>>> getForMe() {
        return ResponseEntity.ok(ApiResponse.success(announcementService.getForCurrentUser()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all announcements (ADMIN only)")
    public ResponseEntity<ApiResponse<PagedResponse<AnnouncementResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                announcementService.getAll(PageRequest.of(page, size))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get announcement by ID")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(announcementService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICAL_DIRECTOR')")
    @Operation(summary = "Create announcement")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> create(
            @Valid @RequestBody AnnouncementRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Announcement created", announcementService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICAL_DIRECTOR')")
    @Operation(summary = "Update announcement")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> update(@PathVariable Long id,
                                                                     @Valid @RequestBody AnnouncementRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Updated", announcementService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete announcement")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        announcementService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.message("Announcement deleted"));
    }
}
