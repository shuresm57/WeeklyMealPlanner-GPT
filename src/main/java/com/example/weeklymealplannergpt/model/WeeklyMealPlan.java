package com.example.weeklymealplannergpt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class WeeklyMealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate weekStartDate;

    @ManyToOne
    @JoinColumn(name = "consumer_id")
    private Consumer consumer;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Meal> meals;

}
