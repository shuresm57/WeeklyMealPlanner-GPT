package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.model.Meal;
import com.example.weeklymealplannergpt.repository.MealRepository;
import com.example.weeklymealplannergpt.service.mealplan.MealCacheService;
import com.example.weeklymealplannergpt.service.mealplan.MealCacheServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MealCacheServiceTest {

    @Mock
    private MealRepository mealRepository;

    @InjectMocks
    private MealCacheServiceImpl mealCacheService;

    @Test
    void initCache_loadsAllMealsFromDatabase() {
        //Arrange
        List<Meal> meals = Arrays.asList(
                createMeal("Pizza"),
                createMeal("Pasta"),
                createMeal("Salad")
        );
        when(mealRepository.findAll()).thenReturn(meals);

        //Act
        mealCacheService.initCache();

        // Assert
        assertThat(mealCacheService.getMealByName("Pasta")).isNotNull();
        assertThat(mealCacheService.getMealByName("Pizza")).isNotNull();
        assertThat(mealCacheService.getMealByName("Salad")).isNotNull();
        verify(mealRepository).findAll();
    }

    @Test
    void getMealByName_whenInCache_returnsMeal() {
        //Arrange
        Meal meal = createMeal("Pizza");
        mealCacheService.addToCache(meal);

        //Act
        Meal result = mealCacheService.getMealByName("Pizza");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(meal);
    }

    @Test
    void getMealByName_whenNotInCache_returnsNull() {
        //Act
        Meal result = mealCacheService.getMealByName("Pizza");

        //Assert
        assertThat(result).isNull();
    }

    @Test
    void addToCache_evictionWorks() {
        // Fill cache to max
        for (int i = 1; i <= MealCacheServiceImpl.getMAX_CACHE_SIZE(); i++) {
            Meal m = new Meal();
            m.setMealName("Meal" + i);
            mealCacheService.addToCache(m);
        }

        // Add one more to trigger eviction
        Meal extra = new Meal();
        extra.setMealName("ExtraMeal");
        mealCacheService.addToCache(extra);

        // Oldest (Meal1) should be evicted
        assertThat(mealCacheService.getMealByName("meal1")).isNull();
        assertThat(mealCacheService.getMealByName("extrameal")).isEqualTo(extra);
    }

    //Hjælpermetode
    private Meal createMeal(String name) {
        Meal meal = new Meal();
        meal.setMealName(name);
        meal.setIngredients(Arrays.asList("ingredient1", "ingredient2"));
        return meal;
    }
}
