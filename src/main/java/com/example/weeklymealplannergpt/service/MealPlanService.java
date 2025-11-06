package com.example.weeklymealplannergpt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

public class MealPlanService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.api.url}")
    private String openAiApiUrl;

    @Value("${openai.model}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final SteamImageService steamImageService;

    public OpenAIService(RestTemplate restTemplate, SteamImageService steamImageService) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
        this.steamImageService = steamImageService;
    }


    private List<GameRecommendation> getSpotOnRecommendations(List<Game> userGames) {
        List<Game> topGames = userGames.stream()
                .sorted(Comparator.comparingInt(Game::getPlaytimeForever).reversed())
                .limit(5)
                .collect(Collectors.toList());

        String gamesList = topGames.stream()
                .map(g -> String.format("%s (%d hours)", g.getName(), g.getPlaytimeForever() / 60))
                .collect(Collectors.joining(", "));

        String prompt = String.format("""
        Based on the user's most played games: %s
        
        Recommend exactly 1 Steam game that is VERY similar to their #1 favorite.
        This should be the PERFECT match - their next favorite game.
        
        Respond with a JSON object containing an array called "recommendations".
        Each recommendation must have:
        - "gameName": exact Steam title (use official name)
        - "reason": detailed, compelling explanation of why this is their perfect next game. Include: gameplay similarities, thematic connections, what makes it special, and why they'll love it based on their favorites. Write 4-5 sentences, be persuasive and enthusiastic. Max 400 characters.
        - "category": "spot-on"
        
        CRITICAL REQUIREMENTS:
        - ONLY popular Steam games (verify they exist)
        - NO Epic Games, GOG, console exclusives, or indie-only games
        - Use exact official Steam store names
        - Games must be currently available for purchase
        - Write a compelling, detailed recommendation that excites the user
        """, gamesList);

        return getRecommendationsFromPrompt(prompt, "spot-on", userGame

}
