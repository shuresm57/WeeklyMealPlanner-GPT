package com.example.weeklymealplannergpt.repository;

import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface WeeklyMealPlanRepository extends JpaRepository<WeeklyMealPlan, Long> {
    List<WeeklyMealPlan> findByConsumerIdOrderByWeekStartDateDesc(UUID consumerId);
    WeeklyMealPlan findByConsumerIdAndWeekStartDate(UUID consumerId, LocalDate weekStartDate);
}
