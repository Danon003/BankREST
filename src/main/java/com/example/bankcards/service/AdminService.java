package com.example.bankcards.service;

import com.example.bankcards.dto.PersonResponseDTO;
import com.example.bankcards.entity.Person;
import com.example.bankcards.repository.PeopleRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final PeopleRepository peopleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    
    public List<PersonResponseDTO> getAllUsers() {
        List<Person> users = peopleRepository.findAll();
        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    
    public Page<PersonResponseDTO> getAllUsers(Pageable pageable) {
        Page<Person> users = peopleRepository.findAll(pageable);
        return users.map(this::convertToDTO);
    }

    
    public PersonResponseDTO getUserById(Integer userId) {
        Person user = peopleRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        return convertToDTO(user);
    }

    @Transactional
    public PersonResponseDTO updateUserRole(Integer userId, String newRole) {
        if (!isValidRole(newRole)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Недопустимая роль. Допустимые значения: ROLE_USER, ROLE_ADMIN");
        }

        Person user = peopleRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));

        user.setRole(newRole);
        Person updatedUser = peopleRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(Integer userId) {
        if (!peopleRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }

        peopleRepository.deleteById(userId);
    }

    
    public List<PersonResponseDTO> getUsersByRole(String role) {
        if (!isValidRole(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Недопустимая роль. Допустимые значения: ROLE_USER, ROLE_ADMIN");
        }

        return peopleRepository.findByRole(role).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PersonResponseDTO createUser(Person person) {
        // Проверяем уникальность username и email
        if (peopleRepository.findByUsername(person.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Пользователь с таким именем уже существует");
        }

        if (peopleRepository.findByEmail(person.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Пользователь с таким email уже существует");
        }

        // Шифруем пароль
        person.setPassword(passwordEncoder.encode(person.getPassword()));
        person.setCreatedAt(LocalDateTime.now());

        // Устанавливаем роль по умолчанию, если не указана
        if (person.getRole() == null) {
            person.setRole("ROLE_USER");
        }

        Person savedUser = peopleRepository.save(person);
        return convertToDTO(savedUser);
    }

    private boolean isValidRole(String role) {
        return "ROLE_USER".equals(role) || "ROLE_ADMIN".equals(role);
    }

    private PersonResponseDTO convertToDTO(Person person) {
        return modelMapper.map(person, PersonResponseDTO.class);
    }
}