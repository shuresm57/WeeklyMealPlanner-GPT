package com.example.weeklymealplannergpt.service.email;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import jakarta.mail.MessagingException;

public interface EmailService {
    void sendWeeklyMeanPlan(Consumer consumer) throws MessagingException;
    void sendMealPlan(Consumer consumer, WeeklyMealPlan mealPlan) throws MessagingException;
}
