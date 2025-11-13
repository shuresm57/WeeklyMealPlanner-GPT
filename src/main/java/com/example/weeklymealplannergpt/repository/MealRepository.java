package com.example.weeklymealplannergpt.repository;

import com.example.weeklymealplannergpt.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRepository extends JpaRepository<Meal, Long> {
}
