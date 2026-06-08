package com.clubmanagement.entity;

import com.clubmanagement.enums.MealType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "daily_meals", indexes = {
    @Index(name = "idx_meal_date", columnList = "mealDate")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyMeal extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate mealDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private MealType mealType;

    @Column(nullable = false, length = 500)
    private String menuDescription;

    private Integer caloricValue;

    @Column(length = 200)
    private String allergenInfo;
}
