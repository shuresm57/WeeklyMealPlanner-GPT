package com.example.weeklymealplannergpt.controller;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.service.consumer.ConsumerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsumerService consumerService;

    @Test
    void testGetProfile() throws Exception {
        Consumer consumer = new Consumer();
        consumer.setEmail("test@example.com");
        consumer.setName("Test User");

        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("email", "test@example.com"),
                "email"
        );

        when(consumerService.findByEmail(any())).thenReturn(consumer);

        mockMvc.perform(get("/api/profile")
                        .with(oauth2Login().oauth2User(oauth2User)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdatePreferences() throws Exception {
        Consumer consumer = new Consumer();
        consumer.setId(UUID.randomUUID());
        consumer.setEmail("test@example.com");
        consumer.setDietType("vegetarian");
        consumer.setAllergies(Set.of("nuts"));
        consumer.setDislikes(Set.of("olives"));

        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("email", "test@example.com"),
                "email"
        );

        when(consumerService.findByEmail(any())).thenReturn(consumer);
        when(consumerService.save(any())).thenReturn(consumer);

        String jsonRequest = """
            {
                "dietType": "vegetarian",
                "allergies": ["nuts"],
                "dislikes": ["olives"]
            }
            """;

        mockMvc.perform(put("/api/profile/preferences")
                        .with(csrf())
                        .with(oauth2Login().oauth2User(oauth2User))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dietType").value("vegetarian"));
    }
}
