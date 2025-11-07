package com.example.bankcards.controller;

import com.example.bankcards.TestSecurityConfig;
import com.example.bankcards.dto.PersonDTO;
import com.example.bankcards.dto.PersonResponseDTO;
import com.example.bankcards.entity.Person;
import com.example.bankcards.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AdminUserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        com.example.bankcards.config.JWTFilter.class,
                        com.example.bankcards.config.SecurityConfig.class
                }
        )
)
@Import(TestSecurityConfig.class)
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private ModelMapper modelMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_Success() throws Exception {
        // Given
        PersonResponseDTO userDTO = new PersonResponseDTO();
        userDTO.setId(1);
        userDTO.setUsername("testuser");

        Page<PersonResponseDTO> page = new PageImpl<>(List.of(userDTO));
        when(adminService.getAllUsers(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/admin/users")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "createdAt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_Success() throws Exception {
        // Given
        PersonResponseDTO userDTO = new PersonResponseDTO();
        userDTO.setId(1);
        userDTO.setUsername("testuser");

        when(adminService.getUserById(1)).thenReturn(userDTO);

        // When & Then
        mockMvc.perform(get("/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_Success() throws Exception {
        // Given
        PersonDTO personDTO = new PersonDTO();
        personDTO.setUsername("newuser");
        personDTO.setEmail("new@example.com");
        personDTO.setPassword("password123");

        PersonResponseDTO responseDTO = new PersonResponseDTO();
        responseDTO.setId(1);
        responseDTO.setUsername("newuser");

        Person person = new Person();
        person.setUsername("newuser");

        when(modelMapper.map(any(PersonDTO.class), eq(Person.class))).thenReturn(person);
        when(adminService.createUser(any(Person.class))).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(personDTO))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_Success() throws Exception {
        // Given
        PersonResponseDTO responseDTO = new PersonResponseDTO();
        responseDTO.setId(1);
        responseDTO.setRole("ROLE_ADMIN");

        when(adminService.updateUserRole(1, "ROLE_ADMIN")).thenReturn(responseDTO);

        // When & Then
        mockMvc.perform(patch("/admin/users/1/role")
                        .param("newRole", "ROLE_ADMIN")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_Success() throws Exception {
        // Given
        doNothing().when(adminService).deleteUser(1);

        // When & Then
        mockMvc.perform(delete("/admin/users/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}