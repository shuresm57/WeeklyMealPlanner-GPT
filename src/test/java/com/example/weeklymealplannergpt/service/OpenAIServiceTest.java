package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.dto.OpenAIResponse;
import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.Meal;
import com.example.weeklymealplannergpt.service.openai.OpenAIServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import org.springframework.core.io.ClassPathResource;

@ExtendWith(MockitoExtension.class)
public class OpenAIServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OpenAIServiceImpl openAIService;

    //Sætter private felter i openAIService før hver test, så API-nøgle, URL og model er tilgængelige.
    //Vi gør det for at initialisere private felter, som normalt bliver sat via Spring (@Value), så testen kan køre uden Spring-kontext.
    @BeforeEach
    void setUp(){
        ReflectionTestUtils.setField(openAIService, "openAiApiKey", "test-key");
        ReflectionTestUtils.setField(openAIService, "openAiApiUrl", "https://api.openai.com/v1/chat/completions");
        ReflectionTestUtils.setField(openAIService, "model", "gpt-3.5-turbo");

        ReflectionTestUtils.setField(openAIService, "promptTemplate",
                new ClassPathResource("chatgpt-prompt.txt"));
    }

    @Test
    void generateMealPlan_withValidResponse_ParsesMeals() throws IOException {
        //Arrange
        Consumer consumer = createTestConsumer();
        String mockResponse = createMockOpenAIResponse();
        OpenAIResponse openAIResponse = createOpenAIResponse(mockResponse);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(OpenAIResponse.class)))
                .thenReturn(ResponseEntity.ok(openAIResponse));

        //Act
        List<Meal> meals = openAIService.generateMealPlan(consumer, 1);

        //Assert
        assertThat(meals).isNotEmpty();
        assertThat(meals).hasSize(5);
        assertThat(meals.getFirst().getMealName()).isNotBlank();
    }

    @Test
    void generateMealPlan_whenOpenAIFails_returnsEmptyList() throws IOException {
        Consumer consumer = createTestConsumer();
        //this is mocking a network failure without making a real HTTP request.
        when(restTemplate.exchange(anyString(), any(), any(), eq(OpenAIResponse.class)))
                .thenThrow(new RestClientException("API Error"));

        //Act
        List<Meal> meals = openAIService.generateMealPlan(consumer, 1);

        //Assert
        assertThat(meals).isEmpty();
    }

    private String createMockOpenAIResponse() {
        return """
            {
                "message": "Here is your meal plan",
                "meals": [
                    {"mealName": "Pasta Primavera", "ingredients": ["pasta", "vegetables"], "imgUrl": ""},
                    {"mealName": "Grilled Salmon", "ingredients": ["salmon", "lemon"], "imgUrl": ""},
                    {"mealName": "Caesar Salad", "ingredients": ["lettuce", "croutons"], "imgUrl": ""},
                    {"mealName": "Chicken Stir Fry", "ingredients": ["chicken", "rice"], "imgUrl": ""},
                    {"mealName": "Vegetable Soup", "ingredients": ["carrots", "celery"], "imgUrl": ""}
                ]
            }
            """;
    }

    private OpenAIResponse createOpenAIResponse(String content) {
        OpenAIResponse response = new OpenAIResponse();
        OpenAIResponse.Choice choice = new OpenAIResponse.Choice();
        OpenAIResponse.Message message = new OpenAIResponse.Message();
        message.setContent(content);
        choice.setMessage(message);
        response.setChoices(Collections.singletonList(choice));
        return response;
    }

    private Consumer createTestConsumer() {
        Consumer consumer = new Consumer();
        consumer.setId(UUID.randomUUID());
        consumer.setDietType("vegetarian");
        consumer.setAllergies(Set.of());
        consumer.setDislikes(Set.of());
        return consumer;
    }

}
