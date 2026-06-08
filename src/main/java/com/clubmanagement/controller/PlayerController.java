package com.clubmanagement.controller;

import com.clubmanagement.dto.common.ApiResponse;
import com.clubmanagement.dto.common.PagedResponse;
import com.clubmanagement.dto.player.ConditionUpdateRequest;
import com.clubmanagement.dto.player.PlayerRequest;
import com.clubmanagement.dto.player.PlayerResponse;
import com.clubmanagement.enums.Position;
import com.clubmanagement.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@Tag(name = "Players", description = "Player management endpoints")
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping
    @Operation(summary = "List players (paginated, with optional search)")
    public ResponseEntity<ApiResponse<PagedResponse<PlayerResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(required = false) Position position,
            @RequestParam(required = false) String nationality,
            @RequestParam(required = false) String name) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(sortBy));
        PagedResponse<PlayerResponse> result = (position != null || nationality != null || name != null)
                ? playerService.search(position, nationality, name, pageable)
                : playerService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get player by ID")
    public ResponseEntity<ApiResponse<PlayerResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(playerService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Create player profile (ADMIN only)")
    public ResponseEntity<ApiResponse<PlayerResponse>> create(@Valid @RequestBody PlayerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Player created", playerService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICAL_DIRECTOR')")
    @Operation(summary = "Update player (ADMIN or TECHNICAL_DIRECTOR)")
    public ResponseEntity<ApiResponse<PlayerResponse>> update(@PathVariable Long id,
                                                               @Valid @RequestBody PlayerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Player updated", playerService.update(id, request)));
    }

    @PatchMapping("/{id}/condition")
    @PreAuthorize("hasRole('PLAYER')")
    @Operation(summary = "Update own condition rating (PLAYER only)")
    public ResponseEntity<ApiResponse<PlayerResponse>> updateCondition(
            @PathVariable Long id,
            @Valid @RequestBody ConditionUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(playerService.updateCondition(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete player (ADMIN only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        playerService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.message("Player deleted"));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICAL_DIRECTOR')")
    @Operation(summary = "Export player list as CSV")
    public ResponseEntity<byte[]> exportCsv() {
        String csv = playerService.exportToCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=players.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }
}
