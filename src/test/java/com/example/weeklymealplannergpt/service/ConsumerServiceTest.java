package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.repository.ConsumerRepository;
import com.example.weeklymealplannergpt.service.consumer.ConsumerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {

    @Mock
    private ConsumerRepository consumerRepository;

    @InjectMocks
    private ConsumerServiceImpl consumerService;

    @Test
    void findByEmail_returnsConsumer() {
        Consumer consumer = new Consumer();
        consumer.setId(UUID.randomUUID());
        consumer.setEmail("test@example.com");
        consumer.setName("John Doe");
        when(consumerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(consumer));

        Consumer result = consumerService.findByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John Doe", result.getName());
    }

    @Test
    void save_returnsConsumer() {
        Consumer consumer = new Consumer();
        consumer.setId(UUID.randomUUID());
        consumer.setEmail("new@example.com");
        consumer.setDietType("vegan");
        when(consumerRepository.save(any(Consumer.class))).thenReturn(consumer);

        Consumer result = consumerService.save(consumer);

        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        assertEquals("vegan", result.getDietType());
    }
}
