package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.dto.TheMealDbResponse;
import com.example.weeklymealplannergpt.service.mealplan.TheMealDbServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TheMealDBServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TheMealDbServiceImpl mealDbService;

    @Test
    void searchMealsByName_returnsListOfMeals() {
        //Arrange
        String searchName = "Pasta";
        TheMealDbResponse response = createMockResponse(3);
        when(restTemplate.getForObject(anyString(), eq(TheMealDbResponse.class)))
                .thenReturn(response);

        //Act
        List<TheMealDbResponse.MealDto> results = mealDbService.searchMealsByName(searchName);

        //Assert
        assertThat(results).hasSize(3);
    }

    @Test
    void searchMealsByName_whenNoResults_returnsEmptyList() {
        //Arrange
        TheMealDbResponse response = new TheMealDbResponse();
        response.setMeals(null);
        when(restTemplate.getForObject(anyString(), eq(TheMealDbResponse.class)))
                .thenReturn(response);

        //Act
        List<TheMealDbResponse.MealDto> results = mealDbService.searchMealsByName("NoResults");

        //Assert
        assertThat(results).isEmpty();
    }


    @Test
    void searchMealsByName_whenApiReturnsNull_returnsEmptyList() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(TheMealDbResponse.class)))
                .thenReturn(null);

        // Act
        List<TheMealDbResponse.MealDto> results = mealDbService.searchMealsByName("Test");

        // Assert
        assertThat(results).isEmpty();
    }

    @Test
    void getMealById_returnsSpecificMeal() {
        // Arrange
        String mealId = "52772";
        TheMealDbResponse response = createMockResponse(1);
        when(restTemplate.getForObject(anyString(), eq(TheMealDbResponse.class)))
                .thenReturn(response);

        // Act
        TheMealDbResponse.MealDto result = mealDbService.getMealById(mealId);

        // Assert
        assertThat(result).isNotNull();
        verify(restTemplate).getForObject(contains(mealId), eq(TheMealDbResponse.class));
    }

    private TheMealDbResponse createMockResponse(int mealCount) {
        TheMealDbResponse response = new TheMealDbResponse();
        List<TheMealDbResponse.MealDto> meals = new ArrayList<>();
        for (int i = 0; i < mealCount; i++) {
            TheMealDbResponse.MealDto meal = new TheMealDbResponse.MealDto();
            meal.setIdMeal("id" + i);
            meal.setStrMeal("Meal " + i);
            meals.add(meal);
        }
        response.setMeals(meals);
        return response;
    }
}
