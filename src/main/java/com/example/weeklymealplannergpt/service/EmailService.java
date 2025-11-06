package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.model.Consumer;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;


@Service
@AllArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    private ThymeleafTemplateEngine templateEngine;

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        javaMailSender.send(message);
    }

    public void sendWeeklyMeanPlan(Consumer consumer, WeeklyMealPlan weeklyMealPlan) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        try {
            helper.setTo(consumer.getEmail());
            helper.setSubject("Your meal plan for this week is ready!");

            Context context = new Context();

            context.setVariable("consumer", consumer);
            context.setVariable("weeklyMealPlan", weeklyMealPlan);
            String html = templateEngine.process("weekly-meal-plan", context);

            helper.setText(html, true); // true = HTML

            helper.setText(html, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException exception){
            throw new RuntimeException(exception);
        }
    }
}
