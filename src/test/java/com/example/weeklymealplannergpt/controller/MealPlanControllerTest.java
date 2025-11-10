package com.example.weeklymealplannergpt.controller;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import com.example.weeklymealplannergpt.service.consumer.ConsumerService;
import com.example.weeklymealplannergpt.service.mealplan.MealPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(MealPlanController.class)
class MealPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MealPlanService mealPlanService;

    @MockBean
    private ConsumerService consumerService;

    @Test
    void testGenerateMealPlan() throws Exception {
        Consumer consumer = new Consumer();
        consumer.setId(UUID.randomUUID());
        consumer.setEmail("test@example.com");

        WeeklyMealPlan plan = new WeeklyMealPlan();
        plan.setId(1L);

        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("email", "test@example.com"),
                "email"
        );

        when(consumerService.findByEmail(any())).thenReturn(consumer);
        when(mealPlanService.generateWeeklyMealPlan(any())).thenReturn(plan);

        mockMvc.perform(post("/api/mealplan/generate")
                        .with(csrf())
                        .with(oauth2Login().oauth2User(oauth2User)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testGetCurrentWeekPlan() throws Exception {
        Consumer consumer = new Consumer();
        consumer.setId(UUID.randomUUID());

        WeeklyMealPlan plan = new WeeklyMealPlan();
        plan.setId(1L);

        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("email", "test@example.com"),
                "email"
        );

        when(consumerService.findByEmail(any())).thenReturn(consumer);
        when(mealPlanService.getCurrentWeekPlan(any())).thenReturn(plan);

        mockMvc.perform(get("/api/mealplan/current")
                        .with(oauth2Login().oauth2User(oauth2User)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testGetPlanHistory() throws Exception {
        Consumer consumer = new Consumer();
        consumer.setId(UUID.randomUUID());

        List<WeeklyMealPlan> plans = new ArrayList<>();
        plans.add(new WeeklyMealPlan());

        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("email", "test@example.com"),
                "email"
        );

        when(consumerService.findByEmail(any())).thenReturn(consumer);
        when(mealPlanService.getPlanHistory(any())).thenReturn(plans);

        mockMvc.perform(get("/api/mealplan/history")
                        .with(oauth2Login().oauth2User(oauth2User)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
