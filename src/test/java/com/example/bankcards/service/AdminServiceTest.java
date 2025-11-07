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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private PeopleRepository peopleRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    @Test
    void getAllUsers_Success() {
        // Given
        Person person = new Person();
        person.setId(1);
        person.setUsername("testuser");

        PersonResponseDTO dto = new PersonResponseDTO();
        dto.setId(1);
        dto.setUsername("testuser");

        when(peopleRepository.findAll()).thenReturn(List.of(person));
        when(modelMapper.map(any(Person.class), eq(PersonResponseDTO.class))).thenReturn(dto);

        // When
        List<PersonResponseDTO> result = adminService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(peopleRepository).findAll();
    }

    @Test
    void getAllUsers_Pageable_Success() {
        // Given
        Person person = new Person();
        person.setId(1);
        person.setUsername("testuser");

        PersonResponseDTO dto = new PersonResponseDTO();
        dto.setId(1);
        dto.setUsername("testuser");

        Page<Person> page = new PageImpl<>(List.of(person));
        when(peopleRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(modelMapper.map(any(Person.class), eq(PersonResponseDTO.class))).thenReturn(dto);

        // When
        Page<PersonResponseDTO> result = adminService.getAllUsers(Pageable.unpaged());

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(peopleRepository).findAll(any(Pageable.class));
    }

    @Test
    void getUserById_Success() {
        // Given
        Person person = new Person();
        person.setId(1);
        person.setUsername("testuser");

        PersonResponseDTO dto = new PersonResponseDTO();
        dto.setId(1);
        dto.setUsername("testuser");

        when(peopleRepository.findById(1)).thenReturn(Optional.of(person));
        when(modelMapper.map(any(Person.class), eq(PersonResponseDTO.class))).thenReturn(dto);

        // When
        PersonResponseDTO result = adminService.getUserById(1);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(peopleRepository).findById(1);
    }

    @Test
    void getUserById_NotFound() {
        // Given
        when(peopleRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adminService.getUserById(1));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void updateUserRole_Success() {
        // Given
        Person person = new Person();
        person.setId(1);
        person.setUsername("testuser");
        person.setRole("ROLE_USER");

        PersonResponseDTO dto = new PersonResponseDTO();
        dto.setId(1);
        dto.setRole("ROLE_ADMIN");

        when(peopleRepository.findById(1)).thenReturn(Optional.of(person));
        when(peopleRepository.save(any(Person.class))).thenReturn(person);
        when(modelMapper.map(any(Person.class), eq(PersonResponseDTO.class))).thenReturn(dto);

        // When
        PersonResponseDTO result = adminService.updateUserRole(1, "ROLE_ADMIN");

        // Then
        assertNotNull(result);
        assertEquals("ROLE_ADMIN", result.getRole());
        verify(peopleRepository).save(person);
        assertEquals("ROLE_ADMIN", person.getRole());
    }

    @Test
    void updateUserRole_InvalidRole() {
        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adminService.updateUserRole(1, "INVALID_ROLE"));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void deleteUser_Success() {
        // Given
        when(peopleRepository.existsById(1)).thenReturn(true);

        // When
        adminService.deleteUser(1);

        // Then
        verify(peopleRepository).deleteById(1);
    }

    @Test
    void deleteUser_NotFound() {
        // Given
        when(peopleRepository.existsById(1)).thenReturn(false);

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adminService.deleteUser(1));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void createUser_Success() {
        // Given
        Person person = new Person();
        person.setUsername("newuser");
        person.setEmail("new@example.com");
        person.setPassword("password");

        Person savedPerson = new Person();
        savedPerson.setId(1);
        savedPerson.setUsername("newuser");

        PersonResponseDTO dto = new PersonResponseDTO();
        dto.setId(1);
        dto.setUsername("newuser");

        when(peopleRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(peopleRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(peopleRepository.save(any(Person.class))).thenReturn(savedPerson);
        when(modelMapper.map(any(Person.class), eq(PersonResponseDTO.class))).thenReturn(dto);

        // When
        PersonResponseDTO result = adminService.createUser(person);

        // Then
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(passwordEncoder).encode("password");
        verify(peopleRepository).save(person);
        assertEquals("encodedPassword", person.getPassword());
        assertEquals("ROLE_USER", person.getRole());
        assertNotNull(person.getCreatedAt());
    }

    @Test
    void createUser_UsernameExists() {
        // Given
        Person person = new Person();
        person.setUsername("existinguser");
        person.setEmail("test@example.com");

        when(peopleRepository.findByUsername("existinguser")).thenReturn(Optional.of(new Person()));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adminService.createUser(person));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void createUser_EmailExists() {
        // Given
        Person person = new Person();
        person.setUsername("newuser");
        person.setEmail("existing@example.com");

        when(peopleRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(peopleRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new Person()));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> adminService.createUser(person));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}