package com.example.weeklymealplannergpt.service.email;

import com.example.weeklymealplannergpt.model.Consumer;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;


@Service
@AllArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    private SpringTemplateEngine templateEngine;

    public void sendWeeklyMeanPlan(Consumer consumer) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        try {
            helper.setTo(consumer.getEmail());
            helper.setSubject("Your meal plan for this week is ready!");

            Context context = new Context();

            context.setVariable("consumer", consumer);
            String html = templateEngine.process("weekly-meal-plan", context);

            helper.setText(html, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException exception){
            throw new RuntimeException(exception);
        }
    }
}
