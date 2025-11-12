package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.dto.TheMealDbResponse;
import com.example.weeklymealplannergpt.service.mealplan.TheMealDbServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class TheMealDbServiceImplTest {

    @Autowired
    private TheMealDbServiceImpl theMealDbServiceImpl;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void testSearchMealsByName() {
        TheMealDbResponse.MealDto mealDto = new TheMealDbResponse.MealDto();
        mealDto.setStrMeal("Chicken Curry");
        mealDto.setStrMealThumb("https://example.com/image.jpg");
        mealDto.setStrIngredient1("Chicken");
        mealDto.setStrIngredient2("Curry Powder");

        TheMealDbResponse response = new TheMealDbResponse();
        response.setMeals(List.of(mealDto));

        when(restTemplate.getForObject(anyString(), eq(TheMealDbResponse.class)))
                .thenReturn(response);

        List<TheMealDbResponse.MealDto> meals = theMealDbServiceImpl.searchMealsByName("Chicken");

        assertNotNull(meals);
        assertEquals(1, meals.size());
        assertEquals("Chicken Curry", meals.get(0).getStrMeal());
    }

    @Test
    void testSearchMealsByIngredient() {
        TheMealDbResponse.MealDto mealDto = new TheMealDbResponse.MealDto();
        mealDto.setStrMeal("Pasta");
        mealDto.setStrMealThumb("https://example.com/pasta.jpg");

        TheMealDbResponse response = new TheMealDbResponse();
        response.setMeals(List.of(mealDto));

        when(restTemplate.getForObject(anyString(), eq(TheMealDbResponse.class)))
                .thenReturn(response);

        List<TheMealDbResponse.MealDto> meals = theMealDbServiceImpl.searchMealsByIngredient("tomato");

        assertNotNull(meals);
        assertEquals(1, meals.size());
        assertEquals("Pasta", meals.get(0).getStrMeal());
    }
}
