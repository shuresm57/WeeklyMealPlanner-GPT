package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.dto.MealPlanResponse;
import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.Meal;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import com.example.weeklymealplannergpt.repository.MealRepository;
import com.example.weeklymealplannergpt.repository.WeeklyMealPlanRepository;
import com.example.weeklymealplannergpt.service.consumer.ConsumerService;
import com.example.weeklymealplannergpt.service.email.EmailService;
import com.example.weeklymealplannergpt.service.mealplan.MealCacheService;
import com.example.weeklymealplannergpt.service.mealplan.MealPlanServiceImpl;
import com.example.weeklymealplannergpt.service.openai.OpenAIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {

    @Mock
    private WeeklyMealPlanRepository weeklyMealPlanRepository;

    @Mock
    private MealRepository mealRepository;

    @Mock
    private OpenAIService openAIService;

    @Mock
    private MealCacheService mealCacheService;

    @Mock
    private ConsumerService consumerService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private MealPlanServiceImpl mealPlanService;

    private Consumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new Consumer();
        consumer.setId(UUID.randomUUID());
        consumer.setEmail("test@example.com");
        consumer.setDietType("vegetarian");
    }

    @Test
    void generateWeeklyMealPlan_createsNewPlan() throws IOException {
        List<Meal> meals = new ArrayList<>();
        Meal meal = new Meal();
        meal.setMealName("Pasta");
        meals.add(meal);
        
        when(consumerService.existsById(any())).thenReturn(true);
        when(openAIService.generateMealPlan(any(), eq(1))).thenReturn(meals);
        when(openAIService.getLastGeneratedMessage()).thenReturn("Plan created");
        when(mealCacheService.getMealByName(any())).thenReturn(null);
        when(mealRepository.save(any())).thenReturn(meal);
        when(weeklyMealPlanRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MealPlanResponse result = mealPlanService.generateWeeklyMealPlan(consumer);

        assertNotNull(result);
        assertNotNull(result.getMealPlan());
        assertEquals("Plan created", result.getMessage());
    }

    @Test
    void generateMonthlyMealPlan_creates20Meals() throws IOException {
        List<Meal> meals = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Meal meal = new Meal();
            meal.setMealName("Meal " + i);
            meals.add(meal);
        }
        
        when(consumerService.existsById(any())).thenReturn(true);
        when(openAIService.generateMealPlan(any(), eq(4))).thenReturn(meals);
        when(openAIService.getLastGeneratedMessage()).thenReturn("Monthly plan created");
        when(mealCacheService.getMealByName(any())).thenReturn(null);
        when(mealRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(weeklyMealPlanRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MealPlanResponse result = mealPlanService.generateMonthlyMealPlan(consumer);

        assertNotNull(result);
        assertEquals(20, result.getMealPlan().getMeals().size());
    }

    @Test
    void getCurrentWeekPlan_returnsExistingPlan() {
        WeeklyMealPlan plan = new WeeklyMealPlan();
        plan.setWeekStartDate(LocalDate.now());
        when(weeklyMealPlanRepository.findByConsumerIdAndWeekStartDate(any(), any())).thenReturn(plan);

        WeeklyMealPlan result = mealPlanService.getCurrentWeekPlan(consumer.getId());

        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getWeekStartDate());
    }

    @Test
    void getPlanHistory_returnsAllPlans() {
        List<WeeklyMealPlan> plans = new ArrayList<>();
        plans.add(new WeeklyMealPlan());
        plans.add(new WeeklyMealPlan());
        when(weeklyMealPlanRepository.findByConsumerIdOrderByWeekStartDateDesc(any())).thenReturn(plans);

        List<WeeklyMealPlan> result = mealPlanService.getPlanHistory(consumer.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
    }
}
