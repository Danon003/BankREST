package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.Person;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private BankCardService bankCardService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransferService transferService;

    private Person createTestUser() {
        Person user = new Person();
        user.setId(1);
        user.setUsername("testuser");
        return user;
    }

    private BankCard createTestCard(Integer id, BigDecimal balance, Person user) {
        BankCard card = new BankCard();
        card.setId(id);
        card.setCardNumberEncrypted("encrypted" + id);
        card.setBalance(balance);
        card.setStatus(BankCard.CardStatus.ACTIVE);
        card.setUser(user);
        return card;
    }

    @Test
    void transferBetweenOwnCards_Success() {
        // Given
        Person user = createTestUser();
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1);
        request.setToCardId(2);
        request.setAmount(BigDecimal.valueOf(100));
        request.setDescription("Test transfer");

        BankCard fromCard = createTestCard(1, BigDecimal.valueOf(500), user);
        BankCard toCard = createTestCard(2, BigDecimal.valueOf(200), user);

        // Создаем транзакцию с установленными картами
        Transaction transaction = new Transaction();
        transaction.setId(1);
        transaction.setFromCard(fromCard);  // Устанавливаем карту отправителя
        transaction.setToCard(toCard);      // Устанавливаем карту получателя
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        transaction.setTransactionDate(LocalDateTime.now());

        when(bankCardService.isCardOwnedByUser(1, user)).thenReturn(true);
        when(bankCardService.isCardOwnedByUser(2, user)).thenReturn(true);
        when(bankCardService.findCardEntityById(1)).thenReturn(fromCard);
        when(bankCardService.findCardEntityById(2)).thenReturn(toCard);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        // Настраиваем моки для getMaskedCardNumber
        when(bankCardService.getMaskedCardNumber("encrypted1")).thenReturn("**** **** **** 1111");
        when(bankCardService.getMaskedCardNumber("encrypted2")).thenReturn("**** **** **** 2222");

        // When
        TransferResponse result = transferService.transferBetweenOwnCards(request, user);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTransactionId());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void transferBetweenOwnCards_CardNotOwned() {
        // Given
        Person user = createTestUser();
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1);
        request.setToCardId(2);

        when(bankCardService.isCardOwnedByUser(1, user)).thenReturn(false);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transferService.transferBetweenOwnCards(request, user));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void transferBetweenOwnCards_InsufficientFunds() {
        // Given
        Person user = createTestUser();
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1);
        request.setToCardId(2);
        request.setAmount(BigDecimal.valueOf(1000));

        BankCard fromCard = createTestCard(1, BigDecimal.valueOf(500), user);
        BankCard toCard = createTestCard(2, BigDecimal.valueOf(200), user);

        when(bankCardService.isCardOwnedByUser(1, user)).thenReturn(true);
        when(bankCardService.isCardOwnedByUser(2, user)).thenReturn(true);
        when(bankCardService.findCardEntityById(1)).thenReturn(fromCard);
        when(bankCardService.findCardEntityById(2)).thenReturn(toCard);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transferService.transferBetweenOwnCards(request, user));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void transferBetweenOwnCards_SameCard() {
        // Given
        Person user = createTestUser();
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1);
        request.setToCardId(1);
        request.setAmount(BigDecimal.valueOf(100));

        BankCard card = createTestCard(1, BigDecimal.valueOf(500), user);

        when(bankCardService.isCardOwnedByUser(1, user)).thenReturn(true);
        when(bankCardService.findCardEntityById(1)).thenReturn(card);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transferService.transferBetweenOwnCards(request, user));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void transferBetweenOwnCards_CardNotActive() {
        // Given
        Person user = createTestUser();
        TransferRequest request = new TransferRequest();
        request.setFromCardId(1);
        request.setToCardId(2);
        request.setAmount(BigDecimal.valueOf(100));

        BankCard fromCard = createTestCard(1, BigDecimal.valueOf(500), user);
        fromCard.setStatus(BankCard.CardStatus.BLOCKED);
        BankCard toCard = createTestCard(2, BigDecimal.valueOf(200), user);

        when(bankCardService.isCardOwnedByUser(1, user)).thenReturn(true);
        when(bankCardService.isCardOwnedByUser(2, user)).thenReturn(true);
        when(bankCardService.findCardEntityById(1)).thenReturn(fromCard);
        when(bankCardService.findCardEntityById(2)).thenReturn(toCard);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transferService.transferBetweenOwnCards(request, user));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}