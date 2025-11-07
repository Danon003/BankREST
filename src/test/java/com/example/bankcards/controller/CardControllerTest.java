package com.example.bankcards.controller;

import com.example.bankcards.TestSecurityConfig;
import com.example.bankcards.dto.BankCardRequest;
import com.example.bankcards.dto.BankCardResponse;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.Person;
import com.example.bankcards.security.JWTUtil;
import com.example.bankcards.security.PersonDetails;
import com.example.bankcards.service.BankCardService;
import com.example.bankcards.service.PersonDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@Import(TestSecurityConfig.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BankCardService bankCardService;

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
    void createCard_Success() throws Exception {
        // Given
        BankCardRequest request = new BankCardRequest();
        request.setCardNumber("1234567890123456");
        request.setCardHolder("TEST USER");
        request.setExpirationDate("12/25");
        request.setCvv("123");
        request.setInitialBalance(BigDecimal.valueOf(1000));

        BankCardResponse response = new BankCardResponse();
        response.setId(1);
        response.setMaskedCardNumber("**** **** **** 3456");
        response.setBalance(BigDecimal.valueOf(1000));

        when(bankCardService.createCard(any(BankCardRequest.class), any(Person.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/cards")
                        .with(user(createMockUser()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.maskedCardNumber").value("**** **** **** 3456"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserCards_Success() throws Exception {
        // Given
        BankCardResponse cardResponse = new BankCardResponse();
        cardResponse.setId(1);
        cardResponse.setMaskedCardNumber("**** **** **** 3456");
        cardResponse.setBalance(BigDecimal.valueOf(1000));

        Page<BankCardResponse> page = new PageImpl<>(List.of(cardResponse));
        when(bankCardService.getUserCards(any(Person.class), any(Pageable.class)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/cards")
                        .with(user(createMockUser()))
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("**** **** **** 3456"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getCard_Success() throws Exception {
        // Given
        BankCardResponse response = new BankCardResponse();
        response.setId(1);
        response.setMaskedCardNumber("**** **** **** 3456");

        when(bankCardService.getCardById(eq(1), any(Person.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get("/cards/1")
                        .with(user(createMockUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void blockCard_Success() throws Exception {
        // Given
        BankCardResponse response = new BankCardResponse();
        response.setId(1);
        response.setStatus(BankCard.CardStatus.BLOCKED);

        when(bankCardService.blockCard(eq(1), any(Person.class)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/cards/1/block")
                        .with(user(createMockUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteCard_Success() throws Exception {
        // Given
        doNothing().when(bankCardService).deleteCard(eq(1), any(Person.class));

        // When & Then
        mockMvc.perform(delete("/cards/1")
                        .with(user(createMockUser())))
                .andExpect(status().isNoContent());
    }
}