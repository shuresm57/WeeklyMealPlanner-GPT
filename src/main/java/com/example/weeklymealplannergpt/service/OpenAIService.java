package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;

public interface OpenAIService {
    WeeklyMealPlan generateWeeklyMealPlan(Consumer consumer);
}
