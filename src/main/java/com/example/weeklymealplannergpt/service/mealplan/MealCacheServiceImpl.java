package com.example.weeklymealplannergpt.service.mealplan;

import com.example.weeklymealplannergpt.model.Meal;
import com.example.weeklymealplannergpt.repository.MealRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class MealCacheServiceImpl implements MealCacheService {

    private final Set<Meal> mealCache = new HashSet<>();

    @Autowired
    private MealRepository mealRepository;

    @PostConstruct
    public void initCache(){
        mealCache.addAll(mealRepository.findAll());
        System.out.println("Meal cache initialized with " + mealCache.size() + " items.");
    }

    public Meal getMealByName(String name){
        return mealCache.stream().filter(m -> m.getMealName().equals(name)).findFirst().orElse(null);
    }

    public void addToCache(Meal meal){
        mealCache.add(meal);
    }
}
