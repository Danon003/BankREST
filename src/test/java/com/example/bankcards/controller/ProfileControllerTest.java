package com.example.bankcards.controller;

import com.example.bankcards.TestSecurityConfig;
import com.example.bankcards.dto.PersonResponseDTO;
import com.example.bankcards.entity.Person;
import com.example.bankcards.security.JWTUtil;
import com.example.bankcards.security.PersonDetails;
import com.example.bankcards.service.PeopleService;
import com.example.bankcards.service.PersonDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileController.class)
@Import(TestSecurityConfig.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PeopleService peopleService;

    @MockitoBean
    private PersonDetailsService personDetailsService;

    @MockitoBean
    private JWTUtil jwtUtil;

    private PersonDetails createMockUser() {
        Person person = new Person();
        person.setId(1);
        person.setUsername("testuser");
        person.setRole("ROLE_USER");
        return new PersonDetails(person);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getProfile_Success() throws Exception {
        // Given
        PersonResponseDTO profile = new PersonResponseDTO();
        profile.setId(1);
        profile.setUsername("testuser");
        profile.setEmail("test@example.com");

        when(peopleService.getUserInfo("testuser")).thenReturn(profile);

        // When & Then
        mockMvc.perform(get("/profile")
                        .with(user(createMockUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"));
    }
}