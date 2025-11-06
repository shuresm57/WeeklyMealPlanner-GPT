package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.repository.ConsumerRepository;
import lombok.Value;
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
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmailService {

    @Mock
    private JavaMailSender javaMailSender;
    @InjectMocks
    private EmailService emailService;

    @BeforeAll
    void beforeAll() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    void beforeEach() {
        String testEmail = "test@example.com";
    }

    @Test
    public void javaMailSender_SendAnEmail(){
        //Arrange
        String subject = "JavaMail Sender";
        String message = "Hello World!";

        //Act
        emailService.sendEmail(testEmail, subject, body);

        //Assert
        verify(javaMailSender, times(1).send(any(SimpleMailMessage.class)));
    }

}
