package com.clubmanagement.controller;

import com.clubmanagement.dto.common.ApiResponse;
import com.clubmanagement.dto.common.PagedResponse;
import com.clubmanagement.dto.match.MatchRequest;
import com.clubmanagement.dto.match.MatchResponse;
import com.clubmanagement.enums.MatchStatus;
import com.clubmanagement.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
@Tag(name = "Matches", description = "Match management endpoints")
public class MatchController {

    private final MatchService matchService;

    @GetMapping
    @Operation(summary = "List all matches (paginated, filterable)")
    public ResponseEntity<ApiResponse<PagedResponse<MatchResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "matchDate") String sortBy,
            @RequestParam(required = false) MatchStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String opponent) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(sortBy));
        PagedResponse<MatchResponse> result = (status != null || from != null || to != null || opponent != null)
                ? matchService.search(status, from, to, opponent, pageable)
                : matchService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming scheduled matches")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getUpcoming() {
        return ResponseEntity.ok(ApiResponse.success(matchService.getUpcoming()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get match by ID")
    public ResponseEntity<ApiResponse<MatchResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(matchService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICAL_DIRECTOR')")
    @Operation(summary = "Create a new match")
    public ResponseEntity<ApiResponse<MatchResponse>> create(@Valid @RequestBody MatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Match created", matchService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICAL_DIRECTOR')")
    @Operation(summary = "Update match")
    public ResponseEntity<ApiResponse<MatchResponse>> update(@PathVariable Long id,
                                                              @Valid @RequestBody MatchRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Match updated", matchService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete match")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        matchService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.message("Match deleted"));
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICAL_DIRECTOR')")
    @Operation(summary = "Export match history as CSV")
    public ResponseEntity<byte[]> exportCsv() {
        String csv = matchService.exportToCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=matches.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes());
    }
}
