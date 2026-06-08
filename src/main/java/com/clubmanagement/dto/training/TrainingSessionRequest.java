package com.clubmanagement.dto.training;

import com.clubmanagement.enums.Intensity;
import com.clubmanagement.enums.TrainingFocus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class TrainingSessionRequest {

    @NotNull(message = "Session date is required")
    private LocalDate sessionDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Focus is required")
    private TrainingFocus focus;

    @NotNull(message = "Intensity is required")
    private Intensity intensity;

    private String notes;
}
