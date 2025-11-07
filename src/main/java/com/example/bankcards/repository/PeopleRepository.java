package com.example.bankcards.repository;

import com.example.bankcards.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PeopleRepository extends JpaRepository<Person, Integer> {
    Optional<Person> findByUsername(String username);
    Optional<Person> findByEmail(String email);
    List<Person> findByRole(String role);
}
