package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.model.Consumer;
import jakarta.mail.MessagingException;

public interface EmailService {
    void sendWeeklyMeanPlan(Consumer consumer) throws MessagingException;
}
