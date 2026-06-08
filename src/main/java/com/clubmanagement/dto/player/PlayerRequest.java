package com.clubmanagement.dto.player;

import com.clubmanagement.enums.Position;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PlayerRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;

    @Min(1) @Max(99)
    private Integer jerseyNumber;

    @NotNull(message = "Position is required")
    private Position position;

    @Size(max = 50)
    private String nationality;

    private LocalDate dateOfBirth;

    @DecimalMin("30.0") @DecimalMax("150.0")
    private Double weightKg;

    @DecimalMin("140.0") @DecimalMax("230.0")
    private Double heightCm;

    private String marketValue;

    private LocalDate contractEndDate;
}
