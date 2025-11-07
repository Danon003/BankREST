package com.example.bankcards.controller;

import com.example.bankcards.dto.PersonResponseDTO;
import com.example.bankcards.security.PersonDetails;
import com.example.bankcards.service.PeopleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Tag(name = "Профиль", description = "API для управления профилем пользователя")
public class ProfileController {

    private final PeopleService peopleService;

    @GetMapping
    @Operation(summary = "Получить информацию о профиле",
            description = "Возвращает информацию о текущем пользователе")
    public ResponseEntity<PersonResponseDTO> getProfile(@AuthenticationPrincipal PersonDetails personDetails) {
        PersonResponseDTO profile = peopleService.getUserInfo(personDetails.getUsername());
        return ResponseEntity.ok(profile);
    }
}