package com.clubmanagement.dto.player;

import com.clubmanagement.enums.Position;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class PlayerResponse {
    private Long id;
    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private Integer jerseyNumber;
    private Position position;
    private String nationality;
    private LocalDate dateOfBirth;
    private Double weightKg;
    private Double heightCm;
    private String marketValue;
    private LocalDate contractEndDate;
    private Integer conditionRating;
    private String conditionNotes;
    private String photoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
