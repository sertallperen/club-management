package com.clubmanagement.dto.leave;

import com.clubmanagement.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveReviewRequest {

    @NotNull(message = "Decision is required")
    private RequestStatus decision;

    private String reviewNote;
}
