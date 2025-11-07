package com.example.bankcards.controller;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.security.PersonDetails;
import com.example.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
@Tag(name = "Переводы", description = "API для переводов между банковскими картами")
public class TransferController {

    private final TransferService transferService;

    @PostMapping("betweenMyCards")
    @Operation(summary = "Перевод между своими картами",
            description = "Выполняет перевод денежных средств между картами текущего пользователя")
    public ResponseEntity<TransferResponse> transferBetweenOwnCards(
            @Valid @RequestBody TransferRequest transferRequest,
            @AuthenticationPrincipal PersonDetails personDetails) {
        TransferResponse response = transferService.transferBetweenOwnCards(transferRequest, personDetails.getPerson());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Перевод между разными картами ",
            description = "Выполняет перевод денежных средств между картами разных пользователей")
    public ResponseEntity<TransferResponse> transferBetweenCards(
            @Valid @RequestBody TransferRequest transferRequest) {
        TransferResponse response = transferService.transferBetweenCards(transferRequest);
        return ResponseEntity.ok(response);
    }
}