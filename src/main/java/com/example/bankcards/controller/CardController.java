package com.example.bankcards.controller;

import com.example.bankcards.dto.BankCardRequest;
import com.example.bankcards.dto.BankCardResponse;
import com.example.bankcards.security.PersonDetails;
import com.example.bankcards.service.BankCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@Tag(name = "Управление картами", description = "API для управления банковскими картами пользователя")
public class CardController {

    private final BankCardService bankCardService;

    @PostMapping
    @Operation(summary = "Создать новую карту",
            description = "Создает новую банковскую карту для текущего пользователя")
    public ResponseEntity<BankCardResponse> createCard(
            @Valid @RequestBody BankCardRequest cardRequest,
            @AuthenticationPrincipal PersonDetails personDetails) {
        return ResponseEntity.ok(bankCardService.createCard(cardRequest, personDetails.getPerson()));
    }

    @GetMapping
    @Operation(summary = "Получить все карты пользователя",
            description = "Возвращает список карт текущего пользователя с пагинацией")
    public ResponseEntity<Page<BankCardResponse>> getUserCards(
            @AuthenticationPrincipal PersonDetails personDetails,
            @Parameter(description = "Номер страницы (начиная с 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Поле для сортировки (createdAt, balance, cardHolder)")
            @RequestParam(defaultValue = "createdAt") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).descending());
        return ResponseEntity.ok(bankCardService.getUserCards(personDetails.getPerson(), pageable));
    }


    @GetMapping("/searchByCardholder")
    @Operation(summary = "Поиск карт пользователя",
            description = "Поиск карт по номеру карты или имени владельца")
    public ResponseEntity<Page<BankCardResponse>> searchUserCards(
            @AuthenticationPrincipal PersonDetails personDetails,
            @Parameter(description = "Поисковый запрос (номер карты или имя владельца)")
            @RequestParam String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<BankCardResponse> cards = bankCardService.getUserCardsWithSearch(
                personDetails.getPerson(), search, pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/active")
    @Operation(summary = "Получить активные карты",
            description = "Возвращает список активных карт пользователя")
    public ResponseEntity<?> getActiveCards(@AuthenticationPrincipal PersonDetails personDetails) {
        return ResponseEntity.ok(bankCardService.getUserActiveCards(personDetails.getPerson()));
    }

    @GetMapping("/{cardId}")
    @Operation(summary = "Получить карту по ID",
            description = "Возвращает информацию о конкретной карте пользователя")
    public ResponseEntity<BankCardResponse> getCard(
            @Parameter(description = "ID карты") @PathVariable Integer cardId,
            @AuthenticationPrincipal PersonDetails personDetails) {
        BankCardResponse card = bankCardService.getCardById(cardId, personDetails.getPerson());
        return ResponseEntity.ok(card);
    }

    @PatchMapping("/{cardId}/block")
    @Operation(summary = "Заблокировать карту",
            description = "Блокирует карту пользователя (только свои карты)")
    public ResponseEntity<BankCardResponse> blockCard(
            @Parameter(description = "ID карты") @PathVariable Integer cardId,
            @AuthenticationPrincipal PersonDetails personDetails) {
        BankCardResponse response = bankCardService.blockCard(cardId, personDetails.getPerson());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{cardId}/activate")
    @Operation(summary = "Активировать карту",
            description = "Активирует заблокированную карту пользователя")
    public ResponseEntity<BankCardResponse> activateCard(
            @Parameter(description = "ID карты") @PathVariable Integer cardId,
            @AuthenticationPrincipal PersonDetails personDetails) {
        BankCardResponse response = bankCardService.activateCard(cardId, personDetails.getPerson());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{cardId}")
    @Operation(summary = "Удалить карту",
            description = "Удаляет карту пользователя (только если баланс = 0)")
    public ResponseEntity<Void> deleteCard(
            @Parameter(description = "ID карты") @PathVariable Integer cardId,
            @AuthenticationPrincipal PersonDetails personDetails) {
        bankCardService.deleteCard(cardId, personDetails.getPerson());
        return ResponseEntity.noContent().build();
    }
}