package com.example.weeklymealplannergpt.service.mealplan;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;

import java.util.List;
import java.util.UUID;

public interface MealPlanService {
    WeeklyMealPlan generateWeeklyMealPlan(Consumer consumer);
    WeeklyMealPlan getCurrentWeekPlan(UUID consumerId);
    List<WeeklyMealPlan> getPlanHistory(UUID consumerId);
}
