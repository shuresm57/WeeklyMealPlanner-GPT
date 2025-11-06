package com.example.weeklymealplannergpt.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String mealName, imgUrl;

    @ElementCollection
    private List<String> ingredients;

}
