package com.example.weeklymealplannergpt.service;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.Meal;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MealPlanService {

        @Autowired
        private OpenAIService openAIService;

        @Autowired
        private ConsumerService consumerService;

        public List<Meal> generateWeeklyMealPlan(Consumer consumer) {
            if(!consumerService.existsById(consumer.getId())) {
                throw new IllegalArgumentException("Consumer does not exist");
            }
            return openAIService.generateWeeklyMealPlan(consumer).getMeals();
        }
}
