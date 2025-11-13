package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import com.example.weeklymealplannergpt.service.email.EmailServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void sendMealPlan_sendsEmail() throws MessagingException {
        // Arrange
        Consumer consumer = createTestConsumer();
        WeeklyMealPlan plan = createTestMealPlan();
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("weekly-meal-plan"), any(Context.class)))
                .thenReturn("<html>Test</html>");

        // Act
        emailService.sendMealPlan(consumer, plan);

        // Assert
        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("weekly-meal-plan"), any(Context.class));
    }

    @Test
    void sendMealPlan_whenMailerFails_throwsRuntimeException() throws MessagingException {
        // Arrange
        Consumer consumer = createTestConsumer();
        WeeklyMealPlan plan = createTestMealPlan();
        MimeMessage mimeMessage = mock(MimeMessage.class);

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any())).thenReturn("<html>Test</html>");
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThatThrownBy(() -> emailService.sendMealPlan(consumer, plan))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send email");
    }

    private Consumer createTestConsumer() {
        Consumer consumer = new Consumer();
        consumer.setEmail("test@example.com");
        consumer.setName("Test User");
        return consumer;
    }

    private WeeklyMealPlan createTestMealPlan() {
        WeeklyMealPlan plan = new WeeklyMealPlan();
        plan.setWeekStartDate(LocalDate.now());
        return plan;
    }
}
