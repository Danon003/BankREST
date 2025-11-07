package com.example.bankcards.service;

import com.example.bankcards.entity.Person;
import com.example.bankcards.repository.PeopleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private PeopleRepository peopleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void register_Success() {
        // Given
        Person person = new Person();
        person.setUsername("newuser");
        person.setEmail("new@example.com");
        person.setPassword("password");

        Person savedPerson = new Person();
        savedPerson.setId(1);
        savedPerson.setUsername("newuser");

        when(peopleRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(peopleRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(peopleRepository.save(any(Person.class))).thenReturn(savedPerson);

        // When
        registrationService.register(person);

        // Then
        verify(peopleRepository).findByUsername("newuser");
        verify(peopleRepository).findByEmail("new@example.com");
        verify(passwordEncoder).encode("password");
        verify(peopleRepository).save(person);
        assertEquals("encodedPassword", person.getPassword());
        assertEquals("ROLE_USER", person.getRole());
        assertNotNull(person.getCreatedAt());
    }

    @Test
    void register_UsernameAlreadyExists() {
        // Given
        Person person = new Person();
        person.setUsername("existinguser");
        person.setEmail("test@example.com");

        when(peopleRepository.findByUsername("existinguser")).thenReturn(Optional.of(new Person()));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> registrationService.register(person));
        assertEquals("Username existinguser already registered", exception.getMessage());
        verify(peopleRepository, never()).save(any(Person.class));
    }

    @Test
    void register_EmailAlreadyExists() {
        // Given
        Person person = new Person();
        person.setUsername("newuser");
        person.setEmail("existing@example.com");

        when(peopleRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(peopleRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new Person()));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> registrationService.register(person));
        assertEquals("Email existing@example.com already registered", exception.getMessage());
        verify(peopleRepository, never()).save(any(Person.class));
    }
}