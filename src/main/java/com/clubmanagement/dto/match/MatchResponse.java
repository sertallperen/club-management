package com.clubmanagement.dto.match;

import com.clubmanagement.enums.MatchStatus;
import com.clubmanagement.enums.VenueType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MatchResponse {
    private Long id;
    private String opponent;
    private LocalDateTime matchDate;
    private String venue;
    private VenueType venueType;
    private String competition;
    private MatchStatus status;
    private Integer homeScore;
    private Integer awayScore;
    private String notes;
    private String broadcastChannel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
