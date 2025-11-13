package com.example.weeklymealplannergpt.service.mealplan;

import com.example.weeklymealplannergpt.dto.MealPlanResponse;
import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;

import java.util.List;
import java.util.UUID;

public interface MealPlanService {
    MealPlanResponse generateWeeklyMealPlan(Consumer consumer);
    MealPlanResponse generateMonthlyMealPlan(Consumer consumer);
    WeeklyMealPlan getCurrentWeekPlan(UUID consumerId);
    List<WeeklyMealPlan> getPlanHistory(UUID consumerId);
    void sendMealPlanByEmail(UUID consumerId, Long mealPlanId);
}
