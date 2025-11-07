package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import com.example.weeklymealplannergpt.repository.ConsumerRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConsumerServiceTest {

    @Mock
    private ConsumerRepository consumerRepository;

    private Consumer consumer;

    @BeforeAll
    public void beforeAll() {
        MockitoAnnotations.openMocks(this);
    }

    @BeforeEach
    void beforeEach() {
        consumer = new Consumer(UUID.randomUUID(), "test@example.com", "John Doe", "OMNIVORE", Set.of("Peanuts", "Dairy"), Set.of("Raisins"), null);
        consumerRepository.save(consumer);
    }

    @Test
    public void consumerRepositoryReturnsEmail(){
        //Arrange
        when(consumerRepository.findByEmail("test@example.com")).thenReturn(Optional.of(consumer));

        //Act
        Optional<Consumer> result = consumerRepository.findByEmail("test@example.com");

        //Assert
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        verify(consumerRepository).findByEmail("test@example.com");
    }


}
