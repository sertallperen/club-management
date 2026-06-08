package com.clubmanagement.dto.injury;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class InjuryReportRequest {

    @NotNull(message = "Player ID is required")
    private Long playerId;

    @NotBlank(message = "Injury type is required")
    private String injuryType;

    private String bodyPart;

    private String description;

    @NotNull(message = "Injury date is required")
    private LocalDate injuryDate;

    private LocalDate expectedReturnDate;
}
