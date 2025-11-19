package com.example.weeklymealplannergpt.controller;

import com.example.weeklymealplannergpt.dto.MealPlanResponse;
import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import com.example.weeklymealplannergpt.service.consumer.ConsumerService;
import com.example.weeklymealplannergpt.service.mealplan.MealPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MealPlanController.class)
class MealPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MealPlanService mealPlanService;

    @MockitoBean
    private ConsumerService consumerService;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    private Consumer consumer;
    private MealPlanResponse response;
    private WeeklyMealPlan plan;

    @BeforeEach
    void setUp() {
        consumer = createTestConsumer();
        plan = new WeeklyMealPlan();
        response = new MealPlanResponse(plan, "Plan created successfully");
    }

    @Test
    void generateMealPlan_createsMonthlyPlan() throws Exception {
        // Arrange
        when(consumerService.findByEmail(anyString())).thenReturn(consumer);
        when(mealPlanService.generateMonthlyMealPlan(any(Consumer.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/mealplan/generate")
                        .with(oauth2Login()
                                .attributes(attrs -> attrs.put("email", "test@example.com")))
                        .with(csrf())
                        .param("type", "monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Plan created successfully"))
                .andExpect(jsonPath("$.mealPlan").exists());
    }

    @Test
    void getCurrentWeekPlan_returnsOK() throws Exception {
        //Arrange
        when(consumerService.findByEmail(anyString())).thenReturn(consumer);
        when(mealPlanService.getCurrentWeekPlan(any(UUID.class))).thenReturn(new WeeklyMealPlan());

        //Act & Assert
        mockMvc.perform(
                        get("/api/mealplan/current")
                                .with(oauth2Login()
                                        .attributes(attrs -> attrs.put("email", "test@example.com")))
                                .with(csrf())
                )
                .andExpect(status().isOk());
    }

    @Test
    void getPlanHistory_returnsList() throws Exception {
        //Arrange
        List<WeeklyMealPlan> weeklyMealPlanList = new ArrayList<>();
        weeklyMealPlanList.add(new WeeklyMealPlan());
        weeklyMealPlanList.add(plan);
        when(consumerService.findByEmail(anyString())).thenReturn(consumer);
        when(mealPlanService.getPlanHistory(any(UUID.class))).thenReturn(weeklyMealPlanList);

        //Act & Assert
        mockMvc.perform(
                        get("/api/mealplan/history")
                                .with(oauth2Login()
                                        .attributes(attrs -> attrs.put("email", "test@example.com")))
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(weeklyMealPlanList.size()));
    }

    @Test
    void sendMealPlanByEmail_success() throws Exception {
        when(consumerService.findByEmail(anyString())).thenReturn(consumer);
        doNothing().when(mealPlanService).sendMealPlanByEmail(any(UUID.class), anyLong());

        mockMvc.perform(
                        post("/api/mealplan/42/email")
                                .with(oauth2Login().attributes(a -> a.put("email", "test@example.com")))
                                .with(csrf())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email sent successfully"));

        verify(mealPlanService).sendMealPlanByEmail(consumer.getId(), 42L);
    }

    private Consumer createTestConsumer() {
        Consumer consumer = new Consumer();
        consumer.setId(UUID.randomUUID());
        consumer.setEmail("test@example.com");
        consumer.setName("Test User");
        consumer.setDietType("omnivore");
        return consumer;
    }
}
