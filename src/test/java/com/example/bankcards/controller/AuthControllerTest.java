package com.example.bankcards.controller;

import com.example.bankcards.TestSecurityConfig;
import com.example.bankcards.dto.AuthenticationDTO;
import com.example.bankcards.dto.PersonDTO;
import com.example.bankcards.entity.Person;
import com.example.bankcards.security.JWTUtil;
import com.example.bankcards.service.PersonDetailsService;
import com.example.bankcards.service.RegistrationService;
import com.example.bankcards.util.PersonValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PersonValidator personValidator;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private PersonDetailsService personDetailsService;

    @Test
    void performRegistration_Success() throws Exception {
        PersonDTO personDTO = new PersonDTO();
        personDTO.setUsername("testuser");
        personDTO.setEmail("test@example.com");
        personDTO.setPassword("password123");

        when(jwtUtil.generateToken(anyString())).thenReturn("test-jwt-token");
        doNothing().when(personValidator).validate(any(Person.class), any(BindingResult.class));

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt-token").value("test-jwt-token"));

        verify(registrationService).register(any(Person.class));
        verify(jwtUtil).generateToken("testuser");
    }

    @Test
    void performRegistration_ValidationError() throws Exception {
        PersonDTO personDTO = new PersonDTO();
        personDTO.setUsername("te");
        personDTO.setEmail("invalid-email");
        personDTO.setPassword("123");

        doAnswer(invocation -> {
            BindingResult bindingResult = invocation.getArgument(1);
            bindingResult.reject("error", "Validation error");
            return null;
        }).when(personValidator).validate(any(Person.class), any(BindingResult.class));

        mockMvc.perform(post("/auth/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Error!"));
    }

    @Test
    void performLogin_Success() throws Exception {
        AuthenticationDTO authDTO = new AuthenticationDTO();
        authDTO.setUsername("testuser");
        authDTO.setPassword("password123");

        when(jwtUtil.generateToken(anyString())).thenReturn("test-jwt-token");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(UsernamePasswordAuthenticationToken.class));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt-token").value("test-jwt-token"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken("testuser");
    }

    @Test
    void performLogin_BadCredentials() throws Exception {
        AuthenticationDTO authDTO = new AuthenticationDTO();
        authDTO.setUsername("testuser");
        authDTO.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Incorrect credentials!"));
    }
}