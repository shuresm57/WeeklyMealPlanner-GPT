package com.example.weeklymealplannergpt.service.mealplan;

import com.example.weeklymealplannergpt.model.Meal;
import com.example.weeklymealplannergpt.repository.MealRepository;
import com.example.weeklymealplannergpt.service.openai.OpenAIService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MealCacheServiceImpl implements MealCacheService {

    private static final Logger logger = LoggerFactory.getLogger(MealCacheServiceImpl.class);
    private final Map<String, Meal> mealCache = new LinkedHashMap<>();

    private final MealRepository mealRepository;

    @Getter
    private static final int MAX_CACHE_SIZE = 1000;

    public MealCacheServiceImpl(MealRepository mealRepository) {
        this.mealRepository = mealRepository;
    }

    @PostConstruct
    public void initCache(){
        mealRepository.findAll().forEach(this::addToCache);
        logger.info("Meal cache initialized with {} items.", mealCache.size());
    }

    public Meal getMealByName(String name){
        if (name == null) return null;
        return mealCache.get(name.toLowerCase());
    }

    /**
     * Tilføjer {@code Meal} til cache.
     * <p>
     *      Hvis {@code Meal} er null eller ikke har noget navn, bliver det ignoreret.
     *      Konvereter meal navn til små bogstaver for case-insenstive søgning.
     *      Fjerner det ældste meal, hvis cachen overskrider maks antal meals (forhindrer memory leaks).
     *      Holder cachen sikker, begrænset i størrelse og muliggør stadig hurtig (O(1)) opslag.
     * </p>
     *
     * @param meal (Meal der skal tilføjes til cache)
     */

    public void addToCache(Meal meal){
        if (meal == null || meal.getMealName() == null) return;

        if (mealCache.size() >= MAX_CACHE_SIZE) {
            String firstKey = mealCache.keySet().iterator().next();
            mealCache.remove(firstKey);
            logger.warn("Meal cache exceeded max size. Evicted {}", firstKey);
        }

        mealCache.put(meal.getMealName().toLowerCase(), meal);
        logger.info("Added meal '{}' to cache.", meal.getMealName());
    }
}
