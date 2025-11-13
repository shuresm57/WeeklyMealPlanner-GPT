package com.example.weeklymealplannergpt.service.email;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;


    public EmailServiceImpl(JavaMailSender javaMailSender, SpringTemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendWeeklyMealPlan(Consumer consumer) throws MessagingException {
        sendMealPlan(consumer, null);
    }

    @Async
    public void sendMealPlan(Consumer consumer, WeeklyMealPlan mealPlan) throws MessagingException {
        
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
        } catch (MessagingException | MailException e) {
            logger.error("Failed to send email to {}", consumer.getEmail(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
