package com.clubmanagement.dto.meal;

import com.clubmanagement.enums.MealType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class DailyMealResponse {
    private Long id;
    private LocalDate mealDate;
    private MealType mealType;
    private String menuDescription;
    private Integer caloricValue;
    private String allergenInfo;
    private LocalDateTime createdAt;
}
