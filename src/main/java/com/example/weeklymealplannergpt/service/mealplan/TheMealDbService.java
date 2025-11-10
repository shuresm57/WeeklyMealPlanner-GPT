package com.example.weeklymealplannergpt.service.mealplan;

import com.example.weeklymealplannergpt.dto.TheMealDbResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class TheMealDbService {

    private final RestTemplate restTemplate;
    private static final String API_BASE_URL = "https://www.themealdb.com/api/json/v1/1";

    public TheMealDbService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<TheMealDbResponse.MealDto> searchMealsByName(String name) {
        String url = API_BASE_URL + "/search.php?s=" + name;
        TheMealDbResponse response = restTemplate.getForObject(url, TheMealDbResponse.class);
        return response != null && response.getMeals() != null ? response.getMeals() : Collections.emptyList();
    }

    public List<TheMealDbResponse.MealDto> searchMealsByIngredient(String ingredient) {
        String url = API_BASE_URL + "/filter.php?i=" + ingredient;
        TheMealDbResponse response = restTemplate.getForObject(url, TheMealDbResponse.class);
        return response != null && response.getMeals() != null ? response.getMeals() : Collections.emptyList();
    }

    public TheMealDbResponse.MealDto getMealById(String id) {
        String url = API_BASE_URL + "/lookup.php?i=" + id;
        TheMealDbResponse response = restTemplate.getForObject(url, TheMealDbResponse.class);
        return response != null && response.getMeals() != null && !response.getMeals().isEmpty() 
            ? response.getMeals().get(0) : null;
    }
}
