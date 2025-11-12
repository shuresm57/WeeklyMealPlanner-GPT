package com.example.weeklymealplannergpt.service.email;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.enabled:false}")
    private boolean emailEnabled;

    public EmailServiceImpl(JavaMailSender javaMailSender, SpringTemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    public void sendWeeklyMeanPlan(Consumer consumer) throws MessagingException {
        sendMealPlan(consumer, null);
    }
    
    public void sendMealPlan(Consumer consumer, WeeklyMealPlan mealPlan) throws MessagingException {
        if (!emailEnabled) {
            logger.warn("Email is disabled. Skipping email to {}", consumer.getEmail());
            throw new MessagingException("Email functionality is disabled. Configure spring.mail.enabled=true and valid SMTP settings.");
        }
        
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        try {
            helper.setTo(consumer.getEmail());
            helper.setSubject("Your meal plan for this week is ready!");

            Context context = new Context();
            context.setVariable("consumer", consumer);
            context.setVariable("mealPlan", mealPlan);
            
            String html = templateEngine.process("weekly-meal-plan", context);

            helper.setText(html, true);
            javaMailSender.send(mimeMessage);
            logger.info("Email sent successfully to {}", consumer.getEmail());
        } catch (MessagingException exception){
            logger.error("Failed to send email to {}", consumer.getEmail(), exception);
            throw new RuntimeException("Failed to send email: " + exception.getMessage(), exception);
        }
    }
}
