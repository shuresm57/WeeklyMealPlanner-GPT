package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

public class OpenAIService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);

    @Value("${OPENAI-KEY}")
    private String OPENAI_KEY;

    @Value("${OPENAI-API}")
    private String openAiApiUrl;

    @Value("${OPENAI-MODEL}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenAIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }


    private List<WeeklyMealPlan> generateWeeklyMealPlan(Consumer consumer) {

        String prompt = String.format("""
                        User preferences:
                        - Allergies: %s
                        - Diet: %s (vegetarian/vegan/omnivore)
                        - Dislikes: %s
                        
                        Create a weekly meal plan for Monday-Friday.
                        
                        For each day provide:
                        - Breakfast
                        - Lunch
                        - Dinner
                        
                        JSON format: {
                            "meals": [
                                {
                                    "day": "Monday",
                                    "breakfast": {"name": "...", "ingredients": [...], "prepTime": "15 min"},
                                    "lunch": {"name": "...", "ingredients": [...], "prepTime": "30 min"},
                                    "dinner": {"name": "...", "ingredients": [...], "prepTime": "45 min"}
                                },
                                ...
                            ]
                        }
                        
                        Requirements:
                        - Avoid all listed allergies
                        - Respect dietary restrictions (%s only)
                        - Avoid disliked ingredients
                        - Balanced nutrition
                        - Variety throughout the week
                        """,
                consumer.getAllergies(),
                consumer.getDietType(),
                consumer.getDislikes(),
                consumer.getDietType());

        return new ArrayList<>();
    }
}
