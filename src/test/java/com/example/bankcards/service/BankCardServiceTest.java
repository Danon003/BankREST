package com.example.bankcards.service;

import com.example.bankcards.dto.BankCardRequest;
import com.example.bankcards.dto.BankCardResponse;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.Person;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.util.CardNumberMasker;
import com.example.bankcards.util.EncryptionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankCardServiceTest {

    @Mock
    private BankCardRepository bankCardRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private CardNumberMasker cardNumberMasker;

    @InjectMocks
    private BankCardService bankCardService;

    private Person createTestUser() {
        Person user = new Person();
        user.setId(1);
        user.setUsername("testuser");
        return user;
    }

    private BankCard createTestCard() {
        BankCard card = new BankCard();
        card.setId(1);
        card.setCardNumberEncrypted("encrypted123");
        card.setCardHolder("TEST USER");
        card.setBalance(BigDecimal.valueOf(1000));
        card.setStatus(BankCard.CardStatus.ACTIVE);
        card.setUser(createTestUser());
        return card;
    }

    @Test
    void createCard_Success() {
        // Given
        BankCardRequest request = new BankCardRequest();
        request.setCardNumber("1234567890123456");
        request.setCardHolder("Test User");
        request.setExpirationDate("12/25");
        request.setCvv("123");
        request.setInitialBalance(BigDecimal.valueOf(1000));

        Person user = createTestUser();
        BankCard savedCard = createTestCard();

        when(encryptionUtil.encrypt("1234567890123456")).thenReturn("encrypted123");
        when(encryptionUtil.encrypt("123")).thenReturn("encryptedCvv");
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(savedCard);
        when(cardNumberMasker.maskCardNumber(anyString(), any())).thenReturn("**** **** **** 3456");

        // When
        BankCardResponse result = bankCardService.createCard(request, user);

        // Then
        assertNotNull(result);
        verify(encryptionUtil).encrypt("1234567890123456");
        verify(encryptionUtil).encrypt("123");
        verify(bankCardRepository).save(any(BankCard.class));
    }

    @Test
    void getCardById_Success() {
        // Given
        BankCard card = createTestCard();
        Person user = createTestUser();

        when(bankCardRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(card));
        when(cardNumberMasker.maskCardNumber(anyString(), any())).thenReturn("**** **** **** 3456");

        // When
        BankCardResponse result = bankCardService.getCardById(1, user);

        // Then
        assertNotNull(result);
        verify(bankCardRepository).findByIdAndUserId(1, 1);
    }

    @Test
    void getCardById_NotFound() {
        // Given
        Person user = createTestUser();
        when(bankCardRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bankCardService.getCardById(1, user));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getUserCards_Success() {
        // Given
        Person user = createTestUser();
        BankCard card = createTestCard();
        Page<BankCard> page = new PageImpl<>(List.of(card));

        when(bankCardRepository.findByUserId(1, Pageable.unpaged())).thenReturn(page);
        when(cardNumberMasker.maskCardNumber(anyString(), any())).thenReturn("**** **** **** 3456");

        // When
        Page<BankCardResponse> result = bankCardService.getUserCards(user, Pageable.unpaged());

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(bankCardRepository).findByUserId(1, Pageable.unpaged());
    }

    @Test
    void blockCard_Success() {
        // Given
        BankCard card = createTestCard();
        Person user = createTestUser();

        when(bankCardRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(card));
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(card);
        when(cardNumberMasker.maskCardNumber(anyString(), any())).thenReturn("**** **** **** 3456");

        // When
        BankCardResponse result = bankCardService.blockCard(1, user);

        // Then
        assertNotNull(result);
        assertEquals(BankCard.CardStatus.BLOCKED, card.getStatus());
        verify(bankCardRepository).save(card);
    }

    @Test
    void blockCard_AlreadyBlocked() {
        // Given
        BankCard card = createTestCard();
        card.setStatus(BankCard.CardStatus.BLOCKED);
        Person user = createTestUser();

        when(bankCardRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(card));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bankCardService.blockCard(1, user));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void activateCard_Success() {
        // Given
        BankCard card = createTestCard();
        card.setStatus(BankCard.CardStatus.BLOCKED);
        Person user = createTestUser();

        when(bankCardRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(card));
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(card);
        when(cardNumberMasker.maskCardNumber(anyString(), any())).thenReturn("**** **** **** 3456");

        // When
        BankCardResponse result = bankCardService.activateCard(1, user);

        // Then
        assertNotNull(result);
        assertEquals(BankCard.CardStatus.ACTIVE, card.getStatus());
        verify(bankCardRepository).save(card);
    }

    @Test
    void deleteCard_Success() {
        // Given
        BankCard card = createTestCard();
        card.setBalance(BigDecimal.ZERO);
        Person user = createTestUser();

        when(bankCardRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(card));

        // When
        bankCardService.deleteCard(1, user);

        // Then
        verify(bankCardRepository).delete(card);
    }

    @Test
    void deleteCard_WithPositiveBalance() {
        // Given
        BankCard card = createTestCard();
        card.setBalance(BigDecimal.valueOf(100));
        Person user = createTestUser();

        when(bankCardRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(card));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> bankCardService.deleteCard(1, user));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void adminBlockCard_Success() {
        // Given
        BankCard card = createTestCard();
        when(bankCardRepository.findById(1)).thenReturn(Optional.of(card));
        when(bankCardRepository.save(any(BankCard.class))).thenReturn(card);
        when(cardNumberMasker.maskCardNumber(anyString(), any())).thenReturn("**** **** **** 3456");

        // When
        BankCardResponse result = bankCardService.adminBlockCard(1);

        // Then
        assertNotNull(result);
        assertEquals(BankCard.CardStatus.BLOCKED, card.getStatus());
        verify(bankCardRepository).save(card);
    }

    @Test
    void isCardOwnedByUser_Success() {
        // Given
        Person user = createTestUser();
        when(bankCardRepository.existsByIdAndUserId(1, 1)).thenReturn(true);

        // When
        boolean result = bankCardService.isCardOwnedByUser(1, user);

        // Then
        assertTrue(result);
        verify(bankCardRepository).existsByIdAndUserId(1, 1);
    }
}