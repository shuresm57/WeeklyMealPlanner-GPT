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
/**
 * findByConsumerIdAndWeekStartDate() - bruges til at hente current week plan (composite index optimerer dette)
 * findByConsumerIdOrderByWeekStartDateDesc() - bruges til history (consumer_id index + weekStartDate sort)
 */
@Table(name = "weekly_meal_plan", indexes = {
        @Index(name = "idx_consumer_week", columnList = "consumer_id, week_start_date"),
        @Index(name = "idx_consumer_id", columnList = "consumer_id"),
        @Index(name = "idx_week_start_date", columnList = "week_start_date")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class WeeklyMealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate weekStartDate;

    @ManyToOne
    @JoinColumn(name = "consumer_id")
    private Consumer consumer;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "weekly_meal_plan_meals",
        joinColumns = @JoinColumn(name = "weekly_meal_plan_id"),
        inverseJoinColumns = @JoinColumn(name = "meal_id")
    )
    private List<Meal> meals;

}
