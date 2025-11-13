package com.example.weeklymealplannergpt.service.openai;

import com.example.weeklymealplannergpt.dto.OpenAIRequest;
import com.example.weeklymealplannergpt.dto.OpenAIResponse;
import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.Meal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import org.springframework.core.io.Resource;
import java.util.*;

@Service
public class OpenAIServiceImpl implements OpenAIService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIServiceImpl.class);

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.api.url}")
    private String openAiApiUrl;

    @Value("${openai.model}")
    private String model;

    @Value("classpath:prompts/chatgpt-prompt.txt")
    private Resource promptTemplate;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Getter
    private String lastGeneratedMessage;

    public OpenAIServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public List<Meal> generateMealPlan(Consumer consumer) throws IOException {
        return generateMealPlan(consumer, 1);
    }

    public List<Meal> generateMealPlan(Consumer consumer, int weeks) throws IOException {
        int totalMeals = weeks * 5;

        String template = Files.readString(promptTemplate.getFile().toPath());
        String prompt = String.format(
                template,
                consumer.getAllergies(),
                consumer.getDietType(),
                consumer.getDislikes(),
                weeks,
                totalMeals,
                weeks,
                totalMeals,
                consumer.getDietType(),
                weeks,
                totalMeals
        );

        return getMealPlanFromPrompt(prompt);
    }

    private List<Meal> getMealPlanFromPrompt(String prompt) {
        try {
            OpenAIRequest request = new OpenAIRequest();
            request.setModel(model);
            request.setTemperature(0.7);
            request.setMax_tokens(1000);
            
            List<OpenAIRequest.Message> messages = new ArrayList<>();
            messages.add(new OpenAIRequest.Message("system", 
                "You are a helpful meal planning assistant. Always respond with valid JSON."));
            messages.add(new OpenAIRequest.Message("user", prompt));
            request.setMessages(messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);

            HttpEntity<OpenAIRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<OpenAIResponse> response = restTemplate.exchange(
                    openAiApiUrl,
                    HttpMethod.POST,
                    entity,
                    OpenAIResponse.class
            );

            if (response.getBody() != null && 
                response.getBody().getChoices() != null && 
                !response.getBody().getChoices().isEmpty()) {
                
                String content = response.getBody().getChoices().get(0).getMessage().getContent();
                return parseMealPlanResponse(content);
            }

        } catch (Exception e) {
            logger.error("Error calling OpenAI API: ", e);
        }

        return new ArrayList<>();
    }

    private List<Meal> parseMealPlanResponse(String jsonContent) {
        List<Meal> meals = new ArrayList<>();

        try {
            String cleanJson = cleanJson(jsonContent);

            Map<String, Object> response = objectMapper.readValue(cleanJson, Map.class);
            List<Map<String, Object>> mealsArray = (List<Map<String, Object>>) response.get("meals");
            
            lastGeneratedMessage = (String) response.get("message");
            
            if (mealsArray != null) {
                logger.info("Successfully parsed {} meals", mealsArray.size());
                
                for (Map<String, Object> mealData : mealsArray) {
                    Meal meal = new Meal();
                    meal.setMealName((String) mealData.get("mealName"));
                    meal.setImgUrl((String) mealData.getOrDefault("imgUrl", ""));
                    
                    List<String> ingredients = (List<String>) mealData.get("ingredients");
                    if (ingredients != null) {
                        meal.setIngredients(ingredients);
                    }
                    
                    meals.add(meal);
                }
            }

        } catch (JsonProcessingException e) {
            logger.error("Error parsing meal plan JSON: ", e);
        }

        return meals;
    }

    private String cleanJson(String jsonContent) {
        jsonContent = jsonContent.trim();
        if (jsonContent.startsWith("```json")) {
            jsonContent = jsonContent.substring(7);
        }
        if (jsonContent.startsWith("```")) {
            jsonContent = jsonContent.substring(3);
        }
        if (jsonContent.endsWith("```")) {
            jsonContent = jsonContent.substring(0, jsonContent.length() - 3);
        }
        return jsonContent;
    }
}
