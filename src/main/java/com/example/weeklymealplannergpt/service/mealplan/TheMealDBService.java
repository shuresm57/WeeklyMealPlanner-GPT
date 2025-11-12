package com.example.weeklymealplannergpt.service.mealplan;

import com.example.weeklymealplannergpt.dto.TheMealDbResponse;

import java.util.List;

public interface TheMealDBService {

    List<TheMealDbResponse.MealDto> searchMealsByName(String name);
    List<TheMealDbResponse.MealDto> searchMealsByIngredient(String ingredient);
    TheMealDbResponse.MealDto getMealById(String id);
}
