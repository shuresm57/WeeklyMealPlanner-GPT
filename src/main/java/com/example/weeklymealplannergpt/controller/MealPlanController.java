package com.example.weeklymealplannergpt.controller;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import com.example.weeklymealplannergpt.service.consumer.ConsumerService;
import com.example.weeklymealplannergpt.service.mealplan.MealPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mealplan")
public class MealPlanController {

    @Autowired
    private MealPlanService mealPlanService;

    @Autowired
    private ConsumerService consumerService;

    @PostMapping("/generate")
    public WeeklyMealPlan generateMealPlan(@AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        Consumer consumer = consumerService.findByEmail(email);
        return mealPlanService.generateWeeklyMealPlan(consumer);
    }

    @GetMapping("/current")
    public ResponseEntity<WeeklyMealPlan> getCurrentWeekPlan(@AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        Consumer consumer = consumerService.findByEmail(email);
        WeeklyMealPlan plan = mealPlanService.getCurrentWeekPlan(consumer.getId());
        
        if (plan == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(plan);
    }

    @GetMapping("/history")
    public List<WeeklyMealPlan> getPlanHistory(@AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        Consumer consumer = consumerService.findByEmail(email);
        return mealPlanService.getPlanHistory(consumer.getId());
    }
}
