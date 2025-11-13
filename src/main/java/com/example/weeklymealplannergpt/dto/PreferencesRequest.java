package com.example.weeklymealplannergpt.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class PreferencesRequest {
    private String dietType;
    private String language;
    private Set<String> allergies;
    private Set<String> dislikes;
}
