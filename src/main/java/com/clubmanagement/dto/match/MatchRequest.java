package com.clubmanagement.dto.match;

import com.clubmanagement.enums.MatchStatus;
import com.clubmanagement.enums.VenueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MatchRequest {

    @NotBlank(message = "Opponent name is required")
    @Size(max = 100)
    private String opponent;

    @NotNull(message = "Match date is required")
    private LocalDateTime matchDate;

    @Size(max = 100)
    private String venue;

    @NotNull(message = "Venue type is required")
    private VenueType venueType;

    @Size(max = 80)
    private String competition;

    private MatchStatus status;

    private Integer homeScore;
    private Integer awayScore;

    @Size(max = 500)
    private String notes;

    private String broadcastChannel;
}
