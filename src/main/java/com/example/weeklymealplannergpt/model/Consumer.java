package com.example.weeklymealplannergpt.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Consumer {

    @Id
    @GeneratedValue
    private UUID id;

    private String email, name, dietType;

    @ElementCollection
    private Set<String> allergies, dislikes;

    @OneToMany(mappedBy = "consumer")
    @JsonIgnore
    private List<WeeklyMealPlan> mealPlans;
}
