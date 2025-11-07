package com.example.bankcards.controller;

import com.example.bankcards.dto.PersonDTO;
import com.example.bankcards.dto.PersonResponseDTO;
import com.example.bankcards.entity.Person;
import com.example.bankcards.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Админ: Управление пользователями", description = "API для административного управления пользователями")
public class AdminUserController {

    private final AdminService adminService;
    private final ModelMapper modelMapper;

    @GetMapping
    @Operation(summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей с пагинацией")
    public ResponseEntity<Page<PersonResponseDTO>> getAllUsers(
            @Parameter(description = "Номер страницы (начиная с 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Поле для сортировки")
            @RequestParam(defaultValue = "createdAt") String sort) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).descending());
        Page<PersonResponseDTO> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/all")
    @Operation(summary = "Получить всех пользователей (без пагинации)",
            description = "Возвращает полный список всех пользователей")
    public ResponseEntity<?> getAllUsersList() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Получить пользователя по ID",
            description = "Возвращает информацию о конкретном пользователе")
    public ResponseEntity<PersonResponseDTO> getUserById(
            @Parameter(description = "ID пользователя") @PathVariable Integer userId) {
        PersonResponseDTO user = adminService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Получить пользователей по роли",
            description = "Возвращает список пользователей с указанной ролью")
    public ResponseEntity<?> getUsersByRole(
            @Parameter(description = "Роль пользователя (ROLE_USER, ROLE_ADMIN)")
            @PathVariable String role) {
        return ResponseEntity.ok(adminService.getUsersByRole(role));
    }

    @PostMapping
    @Operation(summary = "Создать пользователя",
            description = "Создает нового пользователя (администратором)")
    public ResponseEntity<PersonResponseDTO> createUser(@Valid @RequestBody PersonDTO person) {
        PersonResponseDTO createdUser = adminService.createUser(converToPerson(person));
        return ResponseEntity.ok(createdUser);
    }

    @PatchMapping("/{userId}/role")
    @Operation(summary = "Изменить роль пользователя",
            description = "Изменяет роль пользователя на указанную")
    public ResponseEntity<PersonResponseDTO> updateUserRole(
            @Parameter(description = "ID пользователя") @PathVariable Integer userId,
            @Parameter(description = "Новая роль (ROLE_USER, ROLE_ADMIN)")
            @RequestParam String newRole) {
        PersonResponseDTO updatedUser = adminService.updateUserRole(userId, newRole);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Удалить пользователя",
            description = "Удаляет пользователя из системы")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID пользователя") @PathVariable Integer userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    private Person converToPerson(PersonDTO personDTO) {
        return modelMapper.map(personDTO, Person.class);
    }
}