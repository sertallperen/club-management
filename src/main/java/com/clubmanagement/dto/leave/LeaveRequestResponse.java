package com.clubmanagement.dto.leave;

import com.clubmanagement.enums.RequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class LeaveRequestResponse {
    private Long id;
    private String requestedByUsername;
    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;
    private RequestStatus status;
    private String reviewedByUsername;
    private String reviewNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
