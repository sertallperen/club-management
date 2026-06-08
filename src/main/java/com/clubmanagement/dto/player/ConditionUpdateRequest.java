package com.clubmanagement.dto.player;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConditionUpdateRequest {

    @NotNull
    @Min(1) @Max(5)
    private Integer conditionRating;

    private String conditionNotes;
}
