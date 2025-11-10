package com.example.weeklymealplannergpt.controller;

import com.example.weeklymealplannergpt.service.ConsumerService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProfileController {

    @GetMapping("/profile")
    public String profile(OAuth2AuthenticationToken auth) {
        String email = auth.getPrincipal().getAttribute("email");
        return "Logged in as " + email;
    }
}
