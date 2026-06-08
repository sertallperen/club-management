package com.clubmanagement.dto.training;

import com.clubmanagement.enums.Intensity;
import com.clubmanagement.enums.TrainingFocus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
public class TrainingSessionResponse {
    private Long id;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private TrainingFocus focus;
    private Intensity intensity;
    private String notes;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
