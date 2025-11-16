package com.example.weeklymealplannergpt.controller;

import com.example.weeklymealplannergpt.dto.MealPlanResponse;
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
import java.util.Map;

@RestController
@RequestMapping("/api/mealplan")
public class MealPlanController {

    @Autowired
    private MealPlanService mealPlanService;

    @Autowired
    private ConsumerService consumerService;

    @PostMapping("/generate")
    public MealPlanResponse generateMealPlan(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam(defaultValue = "monthly") String type) {
        String email = principal.getAttribute("email");
        Consumer consumer = consumerService.findByEmail(email);
        
        if ("weekly".equalsIgnoreCase(type)) {
            return mealPlanService.generateWeeklyMealPlan(consumer);
        } else {
            return mealPlanService.generateMonthlyMealPlan(consumer);
        }
    }

    @GetMapping("/current")
    public ResponseEntity<WeeklyMealPlan> getCurrentWeekPlan(@AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        Consumer consumer = consumerService.findByEmail(email);
        WeeklyMealPlan plan = mealPlanService.getCurrentWeekPlan(consumer.getId());
        
        if (plan == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok()
                .header("Cache-Control", "private, max-age=300")
                .body(plan);
    }

    @GetMapping("/history")
    public ResponseEntity<List<WeeklyMealPlan>> getPlanHistory(@AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        Consumer consumer = consumerService.findByEmail(email);
        List<WeeklyMealPlan> history = mealPlanService.getPlanHistory(consumer.getId());
        return ResponseEntity.ok()
                .header("Cache-Control", "private, max-age=300")
                .body(history);
    }
    
    @PostMapping("/{mealPlanId}/email")
    public ResponseEntity<Map<String, String>> sendMealPlanByEmail(
            @AuthenticationPrincipal OAuth2User principal,
            @PathVariable Long mealPlanId) {
        String email = principal.getAttribute("email");
        Consumer consumer = consumerService.findByEmail(email);
        
        try {
            mealPlanService.sendMealPlanByEmail(consumer.getId(), mealPlanId);
            return ResponseEntity.ok(Map.of("message", "Email sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to send email: " + e.getMessage()));
        }
    }
}
