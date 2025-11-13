package com.example.weeklymealplannergpt.config;

import com.example.weeklymealplannergpt.model.Consumer;
import com.example.weeklymealplannergpt.repository.ConsumerRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final ConsumerRepository consumerRepository;

    public OAuth2LoginSuccessHandler(ConsumerRepository consumerRepository) {
        this.consumerRepository = consumerRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        consumerRepository.findByEmail(email).orElseGet(() -> {
            Consumer newConsumer = new Consumer();
            newConsumer.setEmail(email);
            newConsumer.setName(name);
            newConsumer.setDietType("omnivore");
            newConsumer.setAllergies(new HashSet<>());
            newConsumer.setDislikes(new HashSet<>());
            return consumerRepository.save(newConsumer);
        });

        setDefaultTargetUrl("/dashboard.html");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
