package com.example.bankcards.controller;

import com.example.bankcards.TestSecurityConfig;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Person;
import com.example.bankcards.security.JWTUtil;
import com.example.bankcards.security.PersonDetails;
import com.example.bankcards.service.PersonDetailsService;
import com.example.bankcards.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
@Import(TestSecurityConfig.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransferService transferService;

    @MockitoBean
    private JWTUtil jwtUtil;

    @MockitoBean
    private PersonDetailsService personDetailsService;

    private PersonDetails createMockUser() {
        Person person = new Person();
        person.setId(1);
        person.setUsername("testuser");
        person.setRole("ROLE_USER");
        return new PersonDetails(person);
    }

    @Test
    @WithMockUser(roles = "USER")
    void transferBetweenOwnCards_Success() throws Exception {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1);
        request.setToCardId(2);
        request.setAmount(BigDecimal.valueOf(100));
        request.setDescription("Test transfer");

        TransferResponse response = new TransferResponse();
        response.setTransactionId(1);
        response.setAmount(BigDecimal.valueOf(100));
        response.setStatus("SUCCESS");

        when(transferService.transferBetweenOwnCards(any(TransferRequest.class), any(Person.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/transfers/betweenMyCards")
                        .with(user(createMockUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(1))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void transferBetweenCards_Success() throws Exception {
        // Given
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1);
        request.setToCardId(2);
        request.setAmount(BigDecimal.valueOf(100));

        TransferResponse response = new TransferResponse();
        response.setTransactionId(1);
        response.setAmount(BigDecimal.valueOf(100));
        response.setStatus("SUCCESS");

        when(transferService.transferBetweenCards(any(TransferRequest.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(1));
    }
}