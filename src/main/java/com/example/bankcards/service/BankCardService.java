package com.example.bankcards.service;

import com.example.bankcards.dto.BankCardRequest;
import com.example.bankcards.dto.BankCardResponse;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.Person;
import com.example.bankcards.repository.BankCardRepository;
import com.example.bankcards.util.CardNumberMasker;
import com.example.bankcards.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BankCardService {

    private final BankCardRepository bankCardRepository;
    private final EncryptionUtil encryptionUtil;
    private final CardNumberMasker cardNumberMasker;

    @Transactional
    public BankCardResponse createCard(BankCardRequest cardRequest, Person user) {
        // Проверяем, не существует ли уже карта с таким номером
        String encryptedCardNumber = encryptionUtil.encrypt(cardRequest.getCardNumber());

        BankCard card = new BankCard();
        card.setCardNumberEncrypted(encryptedCardNumber);
        card.setCardHolder(cardRequest.getCardHolder().toUpperCase());
        card.setExpirationDate(cardRequest.getExpirationDate());
        card.setCvvEncrypted(encryptionUtil.encrypt(cardRequest.getCvv()));
        card.setBalance(cardRequest.getInitialBalance() != null ? cardRequest.getInitialBalance() : BigDecimal.ZERO);
        card.setStatus(BankCard.CardStatus.ACTIVE);
        card.setUser(user);

        BankCard savedCard = bankCardRepository.save(card);
        return convertToResponse(savedCard);
    }

    public BankCardResponse getCardById(Integer cardId, Person user) {
        BankCard card = bankCardRepository.findByIdAndUserId(cardId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Карта не найдена или доступ запрещен"));
        return convertToResponse(card);
    }

    public Page<BankCardResponse> getUserCards(Person user, Pageable pageable) {
        Page<BankCard> cards = bankCardRepository.findByUserId(user.getId(), pageable);
        return cards.map(this::convertToResponse);
    }

    public Page<BankCardResponse> getUserCardsWithSearch(Person user, String search, Pageable pageable) {
        Integer searchId = null;
        try {
            searchId = Integer.parseInt(search);
        } catch (NumberFormatException e) {
            // Если search не число, оставляем null
        }

        Page<BankCard> cards = bankCardRepository.findByUserIdWithSearch(user.getId(), search, searchId, pageable);
        return cards.map(this::convertToResponse);
    }

    public List<BankCardResponse> getUserActiveCards(Person user) {
        List<BankCard> cards = bankCardRepository.findByUserIdAndStatus(user.getId(), BankCard.CardStatus.ACTIVE);
        return cards.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Transactional
    public BankCardResponse blockCard(Integer cardId, Person user) {
        BankCard card = bankCardRepository.findByIdAndUserId(cardId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Карта не найдена или доступ запрещен"));

        if (card.getStatus() == BankCard.CardStatus.BLOCKED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Карта уже заблокирована");
        }

        card.setStatus(BankCard.CardStatus.BLOCKED);
        BankCard updatedCard = bankCardRepository.save(card);
        return convertToResponse(updatedCard);
    }

    @Transactional
    public BankCardResponse activateCard(Integer cardId, Person user) {
        BankCard card = bankCardRepository.findByIdAndUserId(cardId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Карта не найдена или доступ запрещен"));

        if (card.getStatus() == BankCard.CardStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Карта уже активна");
        }

        card.setStatus(BankCard.CardStatus.ACTIVE);
        BankCard updatedCard = bankCardRepository.save(card);
        return convertToResponse(updatedCard);
    }

    public List<BankCardResponse> getAllCards() {
        List<BankCard> cards = bankCardRepository.findAll();
        return cards.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public Page<BankCardResponse> getAllCards(Pageable pageable) {
        Page<BankCard> cards = bankCardRepository.findAll(pageable);
        return cards.map(this::convertToResponse);
    }

    @Transactional
    public BankCardResponse adminBlockCard(Integer cardId) {
        BankCard card = bankCardRepository.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Карта не найдена"));

        card.setStatus(BankCard.CardStatus.BLOCKED);
        BankCard updatedCard = bankCardRepository.save(card);
        return convertToResponse(updatedCard);
    }

    @Transactional
    public BankCardResponse adminActivateCard(Integer cardId) {
        BankCard card = bankCardRepository.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Карта не найдена"));

        card.setStatus(BankCard.CardStatus.ACTIVE);
        BankCard updatedCard = bankCardRepository.save(card);
        return convertToResponse(updatedCard);
    }

    @Transactional
    public void deleteCard(Integer cardId, Person user) {
        BankCard card = bankCardRepository.findByIdAndUserId(cardId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Карта не найдена или доступ запрещен"));

        if (card.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Невозможно удалить карту с положительным балансом");
        }

        bankCardRepository.delete(card);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void adminDeleteCard(Integer cardId) {
        if (!bankCardRepository.existsById(cardId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Карта не найдена");
        }
        bankCardRepository.deleteById(cardId);
    }

    public boolean isCardOwnedByUser(Integer cardId, Person user) {
        return bankCardRepository.existsByIdAndUserId(cardId, user.getId());
    }

    public BankCard findCardEntityById(Integer cardId) {
        return bankCardRepository.findById(cardId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Карта не найдена"));
    }

    public String getMaskedCardNumber(String encryptedCardNumber) {
        return cardNumberMasker.maskCardNumber(encryptedCardNumber, encryptionUtil);
    }

    private BankCardResponse convertToResponse(BankCard card) {
        BankCardResponse response = new BankCardResponse();
        response.setId(card.getId());
        response.setMaskedCardNumber(getMaskedCardNumber(card.getCardNumberEncrypted()));
        response.setCardHolder(card.getCardHolder());
        response.setExpirationDate(card.getExpirationDate());
        response.setBalance(card.getBalance());
        response.setStatus(card.getStatus());
        response.setCreatedAt(card.getCreatedAt());
        response.setUpdatedAt(card.getUpdatedAt());
        return response;
    }
}