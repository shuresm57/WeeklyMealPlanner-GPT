package com.example.weeklymealplannergpt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class WeeklyMealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate weekStartDate;

    @OneToMany
    private List<Meal> meals;

}
