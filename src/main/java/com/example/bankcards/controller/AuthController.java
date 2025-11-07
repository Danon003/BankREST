package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthenticationDTO;
import com.example.bankcards.dto.PersonDTO;
import com.example.bankcards.entity.Person;
import com.example.bankcards.security.JWTUtil;
import com.example.bankcards.service.RegistrationService;
import com.example.bankcards.util.PersonValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "API для регистрации и входа в систему")
public class AuthController {
    private final PersonValidator personValidator;
    private final RegistrationService registrationService;
    private final JWTUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final AuthenticationManager authenticationManager;


    @PostMapping("/registration")
    @Operation(summary = "Регистрация нового пользователя",
            description = "Создает нового пользователя с ролью ROLE_USER")
    public Map<String, String> performRegistration(@RequestBody @Valid PersonDTO personDTO,
                                                   BindingResult bindingResult) {

        Person person = convertToPerson(personDTO);
        personValidator.validate(person, bindingResult);

        if (bindingResult.hasErrors()) {
            return Map.of("message", "Error!");
        }

        registrationService.register(person);
        String token = jwtUtil.generateToken(personDTO.getUsername());

        return Map.of("jwt-token", token);

    }

    @PostMapping("/login")
    @Operation(summary = "Вход в систему",
            description = "Аутентификация пользователя и получение JWT токена")
    public Map<String, String> performLogin(@RequestBody AuthenticationDTO authenticationDTO) {
        UsernamePasswordAuthenticationToken authInputToken =
                new UsernamePasswordAuthenticationToken(authenticationDTO.getUsername(), authenticationDTO.getPassword());

        try {
            authenticationManager.authenticate(authInputToken);
        }catch (BadCredentialsException e){
            return Map.of("message","Incorrect credentials!");
        }

        String token = jwtUtil.generateToken(authenticationDTO.getUsername());
        return Map.of("jwt-token", token);
    }

    public Person convertToPerson(PersonDTO personDTO) {
        return modelMapper.map(personDTO, Person.class);
    }
}

