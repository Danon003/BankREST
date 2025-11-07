package com.example.bankcards.service;

import com.example.bankcards.dto.PersonResponseDTO;
import com.example.bankcards.entity.Person;
import com.example.bankcards.repository.PeopleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PeopleServiceTest {

    @Mock
    private PeopleRepository peopleRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private PeopleService peopleService;

    @Test
    void findByUsername_Success() {
        // Given
        Person person = new Person();
        person.setUsername("testuser");
        when(peopleRepository.findByUsername("testuser")).thenReturn(Optional.of(person));

        // When
        Optional<Person> result = peopleService.findByUsername("testuser");

        // Then
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(peopleRepository).findByUsername("testuser");
    }

    @Test
    void findAll_Success() {
        // Given
        Person person = new Person();
        person.setUsername("testuser");
        when(peopleRepository.findAll()).thenReturn(List.of(person));

        // When
        List<Person> result = peopleService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(peopleRepository).findAll();
    }

    @Test
    void findById_Success() {
        // Given
        Person person = new Person();
        person.setId(1);
        when(peopleRepository.findById(1)).thenReturn(Optional.of(person));

        // When
        Optional<Person> result = peopleService.findById(1);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        verify(peopleRepository).findById(1);
    }

    @Test
    void deleteById_Success() {
        // When
        peopleService.deleteById(1);

        // Then
        verify(peopleRepository).deleteById(1);
    }

    @Test
    void save_Success() {
        // Given
        Person person = new Person();
        person.setUsername("testuser");
        when(peopleRepository.save(person)).thenReturn(person);

        // When
        Person result = peopleService.save(person);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(peopleRepository).save(person);
    }

    @Test
    void getUserInfo_Success() {
        // Given
        Person person = new Person();
        person.setUsername("testuser");
        PersonResponseDTO dto = new PersonResponseDTO();
        dto.setUsername("testuser");

        when(peopleRepository.findByUsername("testuser")).thenReturn(Optional.of(person));
        when(modelMapper.map(any(Person.class), eq(PersonResponseDTO.class))).thenReturn(dto);

        // When
        PersonResponseDTO result = peopleService.getUserInfo("testuser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(peopleRepository).findByUsername("testuser");
    }

    @Test
    void getUserInfo_UserNotFound() {
        // Given
        when(peopleRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(modelMapper.map(any(), eq(PersonResponseDTO.class))).thenReturn(null);

        // When
        PersonResponseDTO result = peopleService.getUserInfo("unknown");

        // Then
        assertNull(result);
    }
}