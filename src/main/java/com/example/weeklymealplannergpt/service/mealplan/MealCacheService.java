package com.example.weeklymealplannergpt.service.mealplan;

import com.example.weeklymealplannergpt.model.Meal;

public interface MealCacheService {

    Meal getMealByName(String name);
    void addToCache(Meal meal);
}
