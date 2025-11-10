package com.example.weeklymealplannergpt.service.openai;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.Meal;

import java.util.List;

public interface OpenAIService {
    List<Meal> generateMealPlan(Consumer consumer);
}
