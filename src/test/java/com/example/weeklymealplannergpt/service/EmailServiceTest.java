package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;
    @InjectMocks
    private EmailService emailService;

    private Consumer consumer;
    private WeeklyMealPlan weeklyMealPlan;

    @BeforeAll
    void beforeAll() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void javaMailSender_SendAnEmail(){
        //Arrange
        String testEmail = "test@example.com";
        String subject = "JavaMail Sender";
        String body = "Hello World!";

        //Act
        emailService.sendEmail(testEmail, subject, body);

        //Assert
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void javaMailSender_SendsAWeeklyMealPlan() {

        consumer = new Consumer(UUID.randomUUID(), "test@example.com", "John Doe", "OMNIVORE", Set.of("Peanuts", "Dairy"));
        weeklyMealPlan = new WeeklyMealPlan(mealPlanService.generateMealPlan(consumer);

        emailService.sendWeeklyMeanPlan(weeklyMealPlan);

        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
);

    }

}
