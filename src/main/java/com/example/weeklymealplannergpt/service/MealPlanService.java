package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.Meal;

import java.util.List;

public interface MealPlanService {
    List<Meal> generateWeeklyMealPlan(Consumer consumer);
}
