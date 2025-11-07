package com.example.bankcards.service;

import com.example.bankcards.entity.Person;
import com.example.bankcards.repository.PeopleRepository;
import com.example.bankcards.security.PersonDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonDetailsServiceTest {

    @Mock
    private PeopleRepository peopleRepository;

    @InjectMocks
    private PersonDetailsService personDetailsService;

    @Test
    void loadUserByUsername_Success() {
        // Given
        Person person = new Person();
        person.setUsername("testuser");
        person.setPassword("password");
        person.setRole("ROLE_USER");

        when(peopleRepository.findByUsername("testuser")).thenReturn(Optional.of(person));

        // When
        UserDetails userDetails = personDetailsService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertInstanceOf(PersonDetails.class, userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        verify(peopleRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        // Given
        when(peopleRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> personDetailsService.loadUserByUsername("unknown"));
        assertEquals("User not found", exception.getMessage());
        verify(peopleRepository).findByUsername("unknown");
    }
}