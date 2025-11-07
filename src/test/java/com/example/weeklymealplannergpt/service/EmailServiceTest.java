package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.Meal;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;
    @Mock
    private SpringTemplateEngine templateEngine;
    @InjectMocks
    private EmailService emailService;


    @BeforeAll
    void beforeAll() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void javaMailSender_SendsAWeeklyMealPlan() throws MessagingException {
        // Arrange
        MimeMessage mimeMessage = new MimeMessage((Session) null);
        Meal meal = new Meal(1L,"Pasta","location",List.of("Pasta"));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("weekly-meal-plan"), any(Context.class)))
                .thenReturn("<html><body>Your meal plan</body></html>");

        WeeklyMealPlan weeklyMealPlan = new WeeklyMealPlan(
                1L,
                LocalDate.now(),
                List.of(meal)
        );

        Consumer consumer = new Consumer(
                UUID.randomUUID(),
                "test@example.com",
                "John Doe",
                "OMNIVORE",
                Set.of("Peanuts", "Dairy"),
                Set.of("Raisins"),
                weeklyMealPlan
        );

        // Act
        emailService.sendWeeklyMeanPlan(consumer);

        // Assert
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

}
