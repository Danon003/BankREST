package com.example.bankcards.service;

import com.example.bankcards.entity.Person;
import com.example.bankcards.repository.PeopleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final PeopleRepository peopleRepository;
    private final PasswordEncoder passwordEncoder;

//    @Transactional
//    public void register(Person person) {
//        peopleRepository.findByUsername(person.getUsername()).orElseThrow(() ->
//                new RuntimeException("Username " + person.getUsername() + " already registered"));
//
//        person.setPassword(passwordEncoder.encode(person.getPassword()));
//        person.setRole("ROLE_USER");
//        peopleRepository.save(person);
//    }

    @Transactional
    public void register(Person person) {
        Optional<Person> existingUser = peopleRepository.findByUsername(person.getUsername());

        if (existingUser.isPresent()) {
            throw new RuntimeException("Username " + person.getUsername() + " already registered");
        }

        Optional<Person> existingEmail = peopleRepository.findByEmail(person.getEmail());

        if (existingEmail.isPresent()) {
            throw new RuntimeException("Email " + person.getEmail() + " already registered");
        }

        // Сохраняем
        person.setPassword(passwordEncoder.encode(person.getPassword()));
        person.setRole("ROLE_USER");
        person.setCreatedAt(LocalDateTime.now());
        Person saved = peopleRepository.save(person);

        log.info("User registered successfully with ID: {}", saved.getId());
    }

}