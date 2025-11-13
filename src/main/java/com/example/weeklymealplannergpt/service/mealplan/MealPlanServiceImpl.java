package com.example.weeklymealplannergpt.service.mealplan;

import com.example.weeklymealplannergpt.dto.MealPlanResponse;
import com.example.weeklymealplannergpt.exception.MealGenerationException;
import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.model.Meal;
import com.example.weeklymealplannergpt.model.WeeklyMealPlan;
import com.example.weeklymealplannergpt.repository.MealRepository;
import com.example.weeklymealplannergpt.repository.WeeklyMealPlanRepository;
import com.example.weeklymealplannergpt.service.consumer.ConsumerService;
import com.example.weeklymealplannergpt.service.email.EmailService;
import com.example.weeklymealplannergpt.service.openai.OpenAIService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MealPlanServiceImpl implements MealPlanService {
    
    private static final Logger logger = LoggerFactory.getLogger(MealPlanServiceImpl.class);

    private final OpenAIService openAIService;
    private final ConsumerService consumerService;
    private final WeeklyMealPlanRepository weeklyMealPlanRepository;
    private final MealRepository mealRepository;
    private final MealCacheService mealCacheService;
    private final EmailService emailService;

    public MealPlanServiceImpl(
            OpenAIService openAIService,
            ConsumerService consumerService,
            WeeklyMealPlanRepository weeklyMealPlanRepository,
            MealRepository mealRepository,
            MealCacheService mealCacheService,
            EmailService emailService) {
        this.openAIService = openAIService;
        this.consumerService = consumerService;
        this.weeklyMealPlanRepository = weeklyMealPlanRepository;
        this.mealRepository = mealRepository;
        this.mealCacheService = mealCacheService;
        this.emailService = emailService;
    }

    private LocalDate getWeekStartDate(){
        return LocalDate.now().with(java.time.DayOfWeek.MONDAY);
    }

    @Transactional
    public MealPlanResponse generateWeeklyMealPlan(Consumer consumer) {
        return generateMealPlan(consumer, 1);
    }
    
    @Transactional
    public MealPlanResponse generateMonthlyMealPlan(Consumer consumer) {
        return generateMealPlan(consumer, 4);
    }
    
    @Transactional
    protected MealPlanResponse generateMealPlan(Consumer consumer, int weeks) {
        logger.info("Generating {}-week meal plan for consumer: {}", weeks, consumer.getId());
        
        if(!consumerService.existsById(consumer.getId())) {
            logger.error("Consumer does not exist: {}", consumer.getId());
            throw new IllegalArgumentException("Consumer does not exist");
        }
        
        try {
            List<Meal> generatedMeals = openAIService.generateMealPlan(consumer, weeks);
            String message = openAIService.getLastGeneratedMessage();
            logger.info("Generated {} meals", generatedMeals.size());
            
            if (generatedMeals == null || generatedMeals.isEmpty()) {
                logger.warn("No meals generated for consumer: {}", consumer.getId());
                throw new MealGenerationException("Could not generate meals. Please try again.");
            }

            List<Meal> finalMeals = new ArrayList<>();

            for (Meal generatedMeal : generatedMeals) {
                Meal existingMeal = mealCacheService.getMealByName(generatedMeal.getMealName());

                if (existingMeal != null) {
                    logger.debug("Using cached meal: {}", existingMeal.getMealName());
                    finalMeals.add(existingMeal);
                } else {
                    logger.debug("Saving new meal to database: {}", generatedMeal.getMealName());
                    Meal savedMeal = mealRepository.save(generatedMeal);
                    mealCacheService.addToCache(savedMeal);
                    finalMeals.add(savedMeal);
                }
            }

            WeeklyMealPlan plan = new WeeklyMealPlan();
            plan.setConsumer(consumer);
            plan.setWeekStartDate(getWeekStartDate());
            plan.setMeals(finalMeals);
            
            WeeklyMealPlan saved = weeklyMealPlanRepository.save(plan);
            logger.info("Successfully saved meal plan with ID: {}", saved.getId());
            
            if (message == null || message.isEmpty()) {
                message = String.format("Your %d-week meal plan with %d meals has been created successfully!", 
                    weeks, finalMeals.size());
            }
            
            return new MealPlanResponse(saved, message);
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
    
    @Transactional
    public void sendMealPlanByEmail(UUID consumerId, Long mealPlanId) {
        logger.info("Sending meal plan {} by email to consumer: {}", mealPlanId, consumerId);
        
        WeeklyMealPlan mealPlan = weeklyMealPlanRepository.findById(mealPlanId)
            .orElseThrow(() -> new IllegalArgumentException("Meal plan not found"));
            
        if (!mealPlan.getConsumer().getId().equals(consumerId)) {
            throw new IllegalArgumentException("Meal plan does not belong to consumer");
        }
        
        try {
            emailService.sendMealPlan(mealPlan.getConsumer(), mealPlan);
            logger.info("Successfully sent meal plan email");
        } catch (MessagingException e) {
            logger.error("Failed to send meal plan email", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
