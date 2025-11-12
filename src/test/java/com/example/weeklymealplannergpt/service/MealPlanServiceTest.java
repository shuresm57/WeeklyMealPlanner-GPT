package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import com.example.weeklymealplannergpt.repository.WeeklyMealPlanRepository;
import com.example.weeklymealplannergpt.service.mealplan.MealPlanService;
import com.example.weeklymealplannergpt.service.mealplan.TheMealDbServiceImpl;
import com.example.weeklymealplannergpt.service.openai.OpenAIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class MealPlanServiceTest {

    @Autowired
    private MealPlanService mealPlanService;

    @MockBean
    private WeeklyMealPlanRepository weeklyMealPlanRepository;

    @MockBean
    private OpenAIService openAIService;

    @MockBean
    private TheMealDbServiceImpl theMealDbServiceImpl;

    private Consumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new Consumer();
        consumer.setId(UUID.randomUUID());
        consumer.setEmail("test@example.com");
        consumer.setDietType("vegetarian");
    }

    @Test
    void testGenerateWeeklyMealPlan() {
        when(openAIService.generateMealPlan(any())).thenReturn(new ArrayList<>());
        when(weeklyMealPlanRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        WeeklyMealPlan plan = mealPlanService.generateWeeklyMealPlan(consumer);

        assertNotNull(plan);
        assertNotNull(plan.getWeekStartDate());
        verify(weeklyMealPlanRepository).save(any());
    }

    @Test
    void testGetCurrentWeekPlan() {
        WeeklyMealPlan plan = new WeeklyMealPlan();
        plan.setWeekStartDate(LocalDate.now());
        when(weeklyMealPlanRepository.findByConsumerIdAndWeekStartDate(any(), any()))
                .thenReturn(plan);

        WeeklyMealPlan result = mealPlanService.getCurrentWeekPlan(consumer.getId());

        assertNotNull(result);
        assertEquals(LocalDate.now(), result.getWeekStartDate());
    }

    @Test
    void testGetPlanHistory() {
        List<WeeklyMealPlan> plans = new ArrayList<>();
        plans.add(new WeeklyMealPlan());
        when(weeklyMealPlanRepository.findByConsumerIdOrderByWeekStartDateDesc(any()))
                .thenReturn(plans);

        List<WeeklyMealPlan> result = mealPlanService.getPlanHistory(consumer.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
