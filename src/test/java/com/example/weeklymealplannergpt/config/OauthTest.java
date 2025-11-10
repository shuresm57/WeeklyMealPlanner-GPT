package com.example.weeklymealplannergpt.config;

import com.example.weeklymealplannergpt.controller.ProfileController;
import com.example.weeklymealplannergpt.service.ConsumerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@Import(SecurityConfig.class)
public class OauthTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    ClientRegistrationRepository clientRegistrationRepository;


    @Test
    void shouldRedirectToLoginPage_whenNotAuthenticated() throws Exception {
        mvc.perform(get("/api/profile")).andExpect(status().is3xxRedirection());
    }

    @Test
    void shouldReturnProfile_whenAuthenticated() throws Exception {
        mvc.perform(get("/api/profile")
                        .with(oauth2Login()
                                .attributes(attrs -> attrs.put("email", "alice@gmail.com"))))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged in as alice@gmail.com"));
    }

}
