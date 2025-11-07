package com.example.bankcards.controller;

import com.example.bankcards.TestSecurityConfig;
import com.example.bankcards.dto.BankCardResponse;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.service.BankCardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AdminCardController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        com.example.bankcards.config.JWTFilter.class,
                        com.example.bankcards.config.SecurityConfig.class
                }
        )
)
@Import(TestSecurityConfig.class)
class AdminCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BankCardService bankCardService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_Success() throws Exception {
        // Given
        BankCardResponse cardResponse = new BankCardResponse();
        cardResponse.setId(1);
        cardResponse.setMaskedCardNumber("**** **** **** 3456");

        Page<BankCardResponse> page = new PageImpl<>(List.of(cardResponse));
        when(bankCardService.getAllCards(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/admin/cards")
                        .with(csrf())
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "createdAt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllCards_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/cards"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminBlockCard_Success() throws Exception {
        // Given
        BankCardResponse response = new BankCardResponse();
        response.setId(1);
        response.setStatus(BankCard.CardStatus.BLOCKED);

        when(bankCardService.adminBlockCard(1)).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/admin/cards/1/block")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminActivateCard_Success() throws Exception {
        // Given
        BankCardResponse response = new BankCardResponse();
        response.setId(1);
        response.setStatus(BankCard.CardStatus.ACTIVE);

        when(bankCardService.adminActivateCard(1)).thenReturn(response);

        // When & Then
        mockMvc.perform(patch("/admin/cards/1/activate")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminDeleteCard_Success() throws Exception {
        // Given
        doNothing().when(bankCardService).adminDeleteCard(1);

        // When & Then
        mockMvc.perform(delete("/admin/cards/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}