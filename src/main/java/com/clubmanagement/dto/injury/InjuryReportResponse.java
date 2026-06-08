package com.clubmanagement.dto.injury;

import com.clubmanagement.enums.InjuryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class InjuryReportResponse {
    private Long id;
    private Long playerId;
    private String playerFullName;
    private String reportedByUsername;
    private String injuryType;
    private String bodyPart;
    private String description;
    private LocalDate injuryDate;
    private LocalDate expectedReturnDate;
    private InjuryStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
