package com.clubmanagement.controller;

import com.clubmanagement.dto.common.ApiResponse;
import com.clubmanagement.service.GalatasarayApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/galatasaray")
@RequiredArgsConstructor
@Tag(name = "Galatasaray Live Data", description = "Live data from TheSportsDB API")
public class GalatasarayController {

    private final GalatasarayApiService apiService;

    @GetMapping("/squad")
    @Operation(summary = "Fetch Galatasaray squad from TheSportsDB")
    public ResponseEntity<ApiResponse<List<GalatasarayApiService.PlayerData>>> getSquad() {
        return ResponseEntity.ok(ApiResponse.success(apiService.fetchSquad()));
    }

    @GetMapping("/matches")
    @Operation(summary = "Fetch upcoming Galatasaray matches from TheSportsDB")
    public ResponseEntity<ApiResponse<List<GalatasarayApiService.MatchData>>> getUpcomingMatches() {
        return ResponseEntity.ok(ApiResponse.success(apiService.fetchUpcomingMatches()));
    }
}
