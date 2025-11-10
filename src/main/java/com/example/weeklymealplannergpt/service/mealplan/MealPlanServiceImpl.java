package com.example.weeklymealplannergpt.service.mealplan;

import com.example.weeklymealplannergpt.exception.MealGenerationException;
import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.Meal;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import com.example.weeklymealplannergpt.repository.WeeklyMealPlanRepository;
import com.example.weeklymealplannergpt.service.consumer.ConsumerService;
import com.example.weeklymealplannergpt.service.openai.OpenAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.UUID;

@Service
public class MealPlanServiceImpl implements MealPlanService {
    
    private static final Logger logger = LoggerFactory.getLogger(MealPlanServiceImpl.class);

    @Autowired
    private OpenAIService openAIService;

    @Autowired
    private ConsumerService consumerService;

    @Autowired
    private WeeklyMealPlanRepository weeklyMealPlanRepository;

    @Transactional
    public WeeklyMealPlan generateWeeklyMealPlan(Consumer consumer) {
        logger.info("Generating meal plan for consumer: {}", consumer.getId());
        
        if(!consumerService.existsById(consumer.getId())) {
            logger.error("Consumer does not exist: {}", consumer.getId());
            throw new IllegalArgumentException("Consumer does not exist");
        }
        
        try {
            List<Meal> meals = openAIService.generateMealPlan(consumer);
            logger.info("Generated {} meals", meals.size());
            
            if (meals == null || meals.isEmpty()) {
                logger.warn("No meals generated for consumer: {}", consumer.getId());
                throw new MealGenerationException("Could not generate meals. Please try again.");
            }
            
            WeeklyMealPlan plan = new WeeklyMealPlan();
            plan.setConsumer(consumer);
            plan.setWeekStartDate(getWeekStartDate());
            plan.setMeals(meals);
            
            WeeklyMealPlan saved = weeklyMealPlanRepository.save(plan);
            logger.info("Successfully saved meal plan with ID: {}", saved.getId());
            
            return saved;
        } catch (MealGenerationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error generating meal plan for consumer: {}", consumer.getId(), e);
            throw new MealGenerationException("Failed to generate meal plan", e);
        }
    }

    public WeeklyMealPlan getCurrentWeekPlan(UUID consumerId) {
        logger.debug("Fetching current week plan for consumer: {}", consumerId);
        return weeklyMealPlanRepository.findByConsumerIdAndWeekStartDate(
            consumerId, getWeekStartDate());
    }

    public List<WeeklyMealPlan> getPlanHistory(UUID consumerId) {
        logger.debug("Fetching plan history for consumer: {}", consumerId);
        return weeklyMealPlanRepository.findByConsumerIdOrderByWeekStartDateDesc(consumerId);
    }

    private LocalDate getWeekStartDate() {
        LocalDate today = LocalDate.now();
        return today.with(ChronoField.DAY_OF_WEEK, 1);
    }
}
