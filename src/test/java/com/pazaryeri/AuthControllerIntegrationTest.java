package com.pazaryeri;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pazaryeri.dto.request.AuthRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_withValidData_shouldReturn201() throws Exception {
        AuthRequest.Register request = new AuthRequest.Register();
        request.setEmail("newuser@test.com");
        request.setPassword("Test123!");
        request.setFirstName("Test");
        request.setLastName("User");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("newuser@test.com"));
    }

    @Test
    void register_withInvalidEmail_shouldReturn400() throws Exception {
        AuthRequest.Register request = new AuthRequest.Register();
        request.setEmail("not-an-email");
        request.setPassword("Test123!");
        request.setFirstName("Test");
        request.setLastName("User");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withWeakPassword_shouldReturn400() throws Exception {
        AuthRequest.Register request = new AuthRequest.Register();
        request.setEmail("test2@test.com");
        request.setPassword("weakpass"); // Büyük harf ve rakam yok
        request.setFirstName("Test");
        request.setLastName("User");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withInvalidCredentials_shouldReturn401() throws Exception {
        AuthRequest.Login request = new AuthRequest.Login();
        request.setEmail("nonexistent@test.com");
        request.setPassword("WrongPass123!");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withoutToken_shouldReturn403() throws Exception {
        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isForbidden());
    }
}
