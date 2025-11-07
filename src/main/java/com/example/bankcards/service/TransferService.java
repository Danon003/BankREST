package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.Person;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransferService {

    private final BankCardService bankCardService;
    private final TransactionRepository transactionRepository;

    @Transactional
    public TransferResponse transferBetweenOwnCards(TransferRequest transferRequest, Person user) {
        // Проверяем, что обе карты принадлежат пользователю
        if (!bankCardService.isCardOwnedByUser(transferRequest.getFromCardId(), user) ||
                !bankCardService.isCardOwnedByUser(transferRequest.getToCardId(), user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Одна из карт не принадлежит пользователю");
        }

        return executeTransfer(transferRequest);
    }

    @Transactional
    public TransferResponse transferBetweenCards(TransferRequest transferRequest) {
        return executeTransfer(transferRequest);
    }

    private TransferResponse executeTransfer(TransferRequest transferRequest) {
        BankCard fromCard = bankCardService.findCardEntityById(transferRequest.getFromCardId());
        BankCard toCard = bankCardService.findCardEntityById(transferRequest.getToCardId());

        validateTransfer(transferRequest, fromCard, toCard);

        fromCard.setBalance(fromCard.getBalance().subtract(transferRequest.getAmount()));
        toCard.setBalance(toCard.getBalance().add(transferRequest.getAmount()));

        Transaction transaction = new Transaction();
        transaction.setFromCard(fromCard);
        transaction.setToCard(toCard);
        transaction.setAmount(transferRequest.getAmount());
        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        transaction.setDescription(transferRequest.getDescription() != null ?
                transferRequest.getDescription() : "Перевод между картами");

        Transaction savedTransaction = transactionRepository.save(transaction);

        log.info("Transfer completed: {} from card {} to card {}",
                transferRequest.getAmount(), fromCard.getId(), toCard.getId());

        return convertToResponse(savedTransaction);
    }

    private void validateTransfer(TransferRequest transferRequest, BankCard fromCard, BankCard toCard) {
        // Проверяем, что карты активны
        if (fromCard.getStatus() != BankCard.CardStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Карта отправителя не активна");
        }
        if (toCard.getStatus() != BankCard.CardStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Карта получателя не активна");
        }

        // Проверяем достаточность средств
        if (fromCard.getBalance().compareTo(transferRequest.getAmount()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Недостаточно средств на карте отправителя. Доступно: " + fromCard.getBalance());
        }

        // Проверяем, что перевод не на ту же карту
        if (fromCard.getId().equals(toCard.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Нельзя переводить на ту же карту");
        }

        // Проверяем, что сумма положительная
        if (transferRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Сумма перевода должна быть положительной");
        }
    }

    private TransferResponse convertToResponse(Transaction transaction) {
        TransferResponse response = new TransferResponse();
        response.setTransactionId(transaction.getId());
        response.setFromCardMaskedNumber(
                bankCardService.getMaskedCardNumber(transaction.getFromCard().getCardNumberEncrypted()));
        response.setToCardMaskedNumber(
                bankCardService.getMaskedCardNumber(transaction.getToCard().getCardNumberEncrypted()));
        response.setAmount(transaction.getAmount());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setStatus(transaction.getStatus().name());
        response.setDescription(transaction.getDescription());
        return response;
    }
}