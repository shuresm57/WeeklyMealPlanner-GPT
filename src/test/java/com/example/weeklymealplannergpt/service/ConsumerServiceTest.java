package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.repository.ConsumerRepository;
import com.example.weeklymealplannergpt.service.consumer.ConsumerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.crossstore.ChangeSetPersister;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsumerServiceTest {

    @Mock
    private ConsumerRepository consumerRepository;

    @InjectMocks
    private ConsumerServiceImpl consumerService;

    @Test
    void findByEmail_WhenExists_ReturnConsumer() {
        // Arrange

        String email = "test@example.com";
        Consumer expectedConsumer = createTestConsumer(email);
        when(consumerRepository.findByEmail(email)).thenReturn(Optional.of(expectedConsumer));

        // Act

        Consumer actualConsumer = consumerService.findByEmail(email);

        // Assert
        assertThat(actualConsumer).isNotNull();
        assertThat(actualConsumer).isEqualTo(expectedConsumer);
        verify(consumerRepository).findByEmail(email);
    }

    @Test
    void findByEmail_WhenNotExists_throwsException() {
        // Arrange
        String email = "notfound@email.com";
        when(consumerRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> consumerService.findByEmail(email))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Consumer not found");}

    @Test
    void findByEmail_returnsConsumer() {
        Consumer testConsumer = createTestConsumer("test@example.com");
        when(consumerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testConsumer));

        Consumer result = consumerService.findByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("Test User", result.getName());
    }

    @Test
    void save_persistsAndReturnsConsumer() {
        // Arrange
        Consumer consumer = createTestConsumer("test@example.com");

        when(consumerRepository.save(any(Consumer.class))).thenReturn(consumer);

        // Act
        Consumer result = consumerService.save(consumer);

        // Assert
        assertNotNull(result, "Returned consumer should not be null");
        assertEquals("test@example.com", result.getEmail());
        assertEquals("omnivore", result.getDietType());

        verify(consumerRepository, times(1)).save(consumer);
    }

    @Test
    void existsById_whenExists_returnsTrue() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(consumerRepository.existsById(id)).thenReturn(true);

        // Act
        boolean exists = consumerService.existsById(id);

        // Assert
        assertThat(exists).isTrue();
    }

    // Hjælpermetode
    private Consumer createTestConsumer(String email) {
        Consumer consumer = new Consumer();
        consumer.setId(UUID.randomUUID());
        consumer.setEmail(email);
        consumer.setName("Test User");
        consumer.setDietType("omnivore");
        return consumer;
    }
}
