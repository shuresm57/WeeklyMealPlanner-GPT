package com.example.weeklymealplannergpt.dto;

import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanResponse {
    private WeeklyMealPlan mealPlan;
    private String message;
}
