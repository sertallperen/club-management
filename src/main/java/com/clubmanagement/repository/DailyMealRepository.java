package com.clubmanagement.repository;

import com.clubmanagement.entity.DailyMeal;
import com.clubmanagement.enums.MealType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyMealRepository extends JpaRepository<DailyMeal, Long> {

    Optional<DailyMeal> findByIdAndDeletedFalse(Long id);

    Page<DailyMeal> findAllByDeletedFalse(Pageable pageable);

    List<DailyMeal> findByMealDateAndDeletedFalseOrderByMealTypeAsc(LocalDate date);

    boolean existsByMealDateAndMealTypeAndDeletedFalse(LocalDate date, MealType type);
}
