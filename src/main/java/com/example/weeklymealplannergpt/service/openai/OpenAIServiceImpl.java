package com.example.weeklymealplannergpt.service.openai;

import com.example.weeklymealplannergpt.dto.OpenAIRequest;
import com.example.weeklymealplannergpt.dto.OpenAIResponse;
import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.Meal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    private String lastGeneratedMessage;

    public OpenAIServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }
    
    public String getLastGeneratedMessage() {
        return lastGeneratedMessage;
    }


    public List<Meal> generateMealPlan(Consumer consumer) {
        return generateMealPlan(consumer, 1);
    }

    public List<Meal> generateMealPlan(Consumer consumer, int weeks) {
        int totalMeals = weeks * 5;
        
        String prompt = String.format("""
                        
                        You MUST respond ONLY with valid JSON. No markdown, no code blocks, just pure JSON starting with { and ending with }."
                        
                        User preferences:
                        - Allergies: %s
                        - Diet: %s (vegetarian/vegan/omnivore)
                        - Dislikes: %s
                        
                        Create a %d-week dinner plan (%d dinners total, Monday-Friday each week).
                        
                        JSON format: {
                            "meals": [
                                {
                                    "mealName": "Week 1 Monday - Spaghetti Bolognese",
                                    "ingredients": ["pasta", "ground beef", "tomato sauce", "onions"],
                                    "imgUrl": ""
                                },
                                {
                                    "mealName": "Week 1 Tuesday - Chicken Stir Fry",
                                    "ingredients": ["chicken", "vegetables", "soy sauce", "rice"],
                                    "imgUrl": ""
                                },
                                ...
                            ],
                            "message": "Your %d-week meal plan is ready! I've created %d delicious and varied meals tailored to your preferences."
                        }
                        
                        Requirements:
                        - Avoid all listed allergies
                        - Respect dietary restrictions (%s only)
                        - Avoid disliked ingredients
                        - Balanced nutrition
                        - Variety throughout all %d weeks
                        - Provide exactly %d dinner meals
                        - Include a friendly confirmation message
                        
                        IMPORTANT: Respond with ONLY the JSON object. No markdown code blocks (```), no explanations, just the pure JSON.
                        """,
                consumer.getAllergies(),
                consumer.getDietType(),
                consumer.getDislikes(),
                weeks,
                totalMeals,
                weeks,
                totalMeals,
                consumer.getDietType(),
                weeks,
                totalMeals);

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
            String cleanJson = jsonContent.trim();
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            }
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();

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
}
