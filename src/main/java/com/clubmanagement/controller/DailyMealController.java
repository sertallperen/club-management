package com.clubmanagement.controller;

import com.clubmanagement.dto.common.ApiResponse;
import com.clubmanagement.dto.common.PagedResponse;
import com.clubmanagement.dto.meal.DailyMealRequest;
import com.clubmanagement.dto.meal.DailyMealResponse;
import com.clubmanagement.service.DailyMealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
@Tag(name = "Daily Meals", description = "Meal plan management")
public class DailyMealController {

    private final DailyMealService mealService;

    @GetMapping
    @Operation(summary = "List all meals (paginated)")
    public ResponseEntity<ApiResponse<PagedResponse<DailyMealResponse>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(mealService.getAll(PageRequest.of(page, size))));
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's meals")
    public ResponseEntity<ApiResponse<List<DailyMealResponse>>> getToday() {
        return ResponseEntity.ok(ApiResponse.success(mealService.getToday()));
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get meals for a specific date")
    public ResponseEntity<ApiResponse<List<DailyMealResponse>>> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(mealService.getByDate(date)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get meal by ID")
    public ResponseEntity<ApiResponse<DailyMealResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(mealService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICAL_DIRECTOR', 'ASSISTANT_COACH')")
    @Operation(summary = "Create a meal entry")
    public ResponseEntity<ApiResponse<DailyMealResponse>> create(@Valid @RequestBody DailyMealRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Meal created", mealService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICAL_DIRECTOR', 'ASSISTANT_COACH')")
    @Operation(summary = "Update meal entry")
    public ResponseEntity<ApiResponse<DailyMealResponse>> update(@PathVariable Long id,
                                                                   @Valid @RequestBody DailyMealRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Meal updated", mealService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete meal entry")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        mealService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.message("Meal deleted"));
    }
}
