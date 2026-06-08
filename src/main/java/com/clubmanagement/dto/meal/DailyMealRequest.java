package com.clubmanagement.dto.meal;

import com.clubmanagement.enums.MealType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DailyMealRequest {

    @NotNull(message = "Meal date is required")
    private LocalDate mealDate;

    @NotNull(message = "Meal type is required")
    private MealType mealType;

    @NotBlank(message = "Menu description is required")
    private String menuDescription;

    private Integer caloricValue;

    private String allergenInfo;
}
