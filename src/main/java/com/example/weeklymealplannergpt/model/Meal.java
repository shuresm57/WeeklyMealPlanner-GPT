package com.example.weeklymealplannergpt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String mealName, imgUrl;

    @ElementCollection
    private List<String> ingredients;

    /**
     * Denne metode siger, at hvis to Meal objekter hedder det samme
     * Så er deres hash-værdi lig med hinanden. Altså kan der ikke eksistere to af samme navn.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Meal)) return false;
        Meal meal = (Meal) o;
        return Objects.equals(mealName, meal.mealName);
    }

    /**
     * Sikrer at Meal objekter har den samme hash-værdi.
     */
    @Override
    public int hashCode() {
        return Objects.hash(mealName);
    }
}
