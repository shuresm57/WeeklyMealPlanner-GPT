package com.example.weeklymealplannergpt.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
public class WeeklyMealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate weekStartDate;

    @OneToMany
    private List<Meal> days;
}
