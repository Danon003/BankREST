package com.example.bankcards.controller;

import com.example.bankcards.dto.BankCardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.service.BankCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Админ: Управление картами", description = "API для административного управления всеми банковскими картами")
public class AdminCardController {

    private final BankCardService bankCardService;

    @Autowired
    public AdminCardController(BankCardService bankCardService) {
        this.bankCardService = bankCardService;
    }

    @GetMapping
    @Operation(summary = "Получить все карты",
            description = "Возвращает список всех карт в системе с пагинацией (только для администраторов)")
    public ResponseEntity<Page<BankCardResponse>> getAllCards(
            @Parameter(description = "Номер страницы (начиная с 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Поле для сортировки")
            @RequestParam(defaultValue = "createdAt") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).descending());
        Page<BankCardResponse> cards = bankCardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @PatchMapping("/{cardId}/block")
    @Operation(summary = "Заблокировать карту (админ)",
            description = "Блокирует любую карту в системе")
    public ResponseEntity<BankCardResponse> adminBlockCard(
            @Parameter(description = "ID карты") @PathVariable Integer cardId) {
        BankCardResponse response = bankCardService.adminBlockCard(cardId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{cardId}/activate")
    @Operation(summary = "Активировать карту (админ)",
            description = "Активирует любую карту в системе")
    public ResponseEntity<BankCardResponse> adminActivateCard(
            @Parameter(description = "ID карты") @PathVariable Integer cardId) {
        BankCardResponse response = bankCardService.adminActivateCard(cardId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{cardId}")
    @Operation(summary = "Удалить карту (админ)",
            description = "Удаляет любую карту из системы")
    public ResponseEntity<Void> adminDeleteCard(
            @Parameter(description = "ID карты") @PathVariable Integer cardId) {
        bankCardService.adminDeleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}