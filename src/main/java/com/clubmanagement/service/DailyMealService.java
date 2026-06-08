package com.clubmanagement.service;

import com.clubmanagement.dto.common.PagedResponse;
import com.clubmanagement.dto.meal.DailyMealRequest;
import com.clubmanagement.dto.meal.DailyMealResponse;
import com.clubmanagement.entity.DailyMeal;
import com.clubmanagement.exception.BusinessRuleException;
import com.clubmanagement.exception.ResourceNotFoundException;
import com.clubmanagement.repository.DailyMealRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyMealService {

    private final DailyMealRepository mealRepository;

    public PagedResponse<DailyMealResponse> getAll(Pageable pageable) {
        return PagedResponse.of(mealRepository.findAllByDeletedFalse(pageable).map(this::toResponse));
    }

    public List<DailyMealResponse> getToday() {
        return mealRepository.findByMealDateAndDeletedFalseOrderByMealTypeAsc(LocalDate.now())
                .stream().map(this::toResponse).toList();
    }

    public List<DailyMealResponse> getByDate(LocalDate date) {
        return mealRepository.findByMealDateAndDeletedFalseOrderByMealTypeAsc(date)
                .stream().map(this::toResponse).toList();
    }

    public DailyMealResponse getById(Long id) {
        return toResponse(findActive(id));
    }

    @Transactional
    public DailyMealResponse create(DailyMealRequest request) {
        if (mealRepository.existsByMealDateAndMealTypeAndDeletedFalse(
                request.getMealDate(), request.getMealType())) {
            throw new BusinessRuleException(
                    "A meal of type " + request.getMealType() + " already exists for " + request.getMealDate());
        }
        DailyMeal meal = DailyMeal.builder()
                .mealDate(request.getMealDate())
                .mealType(request.getMealType())
                .menuDescription(request.getMenuDescription())
                .caloricValue(request.getCaloricValue())
                .allergenInfo(request.getAllergenInfo())
                .build();
        return toResponse(mealRepository.save(meal));
    }

    @Transactional
    public DailyMealResponse update(Long id, DailyMealRequest request) {
        DailyMeal meal = findActive(id);
        meal.setMealDate(request.getMealDate());
        meal.setMealType(request.getMealType());
        meal.setMenuDescription(request.getMenuDescription());
        meal.setCaloricValue(request.getCaloricValue());
        meal.setAllergenInfo(request.getAllergenInfo());
        return toResponse(mealRepository.save(meal));
    }

    @Transactional
    public void softDelete(Long id) {
        DailyMeal meal = findActive(id);
        meal.setDeleted(true);
        mealRepository.save(meal);
    }

    private DailyMeal findActive(Long id) {
        return mealRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("DailyMeal", id));
    }

    public DailyMealResponse toResponse(DailyMeal m) {
        return DailyMealResponse.builder()
                .id(m.getId())
                .mealDate(m.getMealDate())
                .mealType(m.getMealType())
                .menuDescription(m.getMenuDescription())
                .caloricValue(m.getCaloricValue())
                .allergenInfo(m.getAllergenInfo())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
