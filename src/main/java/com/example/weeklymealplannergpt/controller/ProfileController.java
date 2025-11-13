package com.example.weeklymealplannergpt.controller;

import com.example.weeklymealplannergpt.dto.PreferencesRequest;
import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.service.consumer.ConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ProfileController {

    @Autowired
    private ConsumerService consumerService;

    @GetMapping("/profile")
    public Consumer profile(@AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        return consumerService.findByEmail(email);
    }

    @PutMapping("/profile/preferences")
    public Consumer updatePreferences(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody PreferencesRequest request) {
        String email = principal.getAttribute("email");
        Consumer consumer = consumerService.findByEmail(email);
        
        consumer.setDietType(request.getDietType());
        consumer.setAllergies(request.getAllergies());
        consumer.setDislikes(request.getDislikes());
        if (request.getLanguage() != null) {
            consumer.setLanguage(request.getLanguage());
        }
        
        return consumerService.save(consumer);
    }
}
