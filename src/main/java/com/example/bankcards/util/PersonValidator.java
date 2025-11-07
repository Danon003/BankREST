package com.example.bankcards.util;


import com.example.bankcards.entity.Person;
import com.example.bankcards.service.PeopleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;


@Component
@Slf4j
public class PersonValidator implements Validator {
    private final PeopleService peopleService;

    @Autowired
    public PersonValidator(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Person.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Person person = (Person) target;

        log.info("PersonValidator checking username: {}", person.getUsername());
        Optional<Person> existingUser = peopleService.findByUsername(person.getUsername());
        log.info("PersonValidator - username {} exists: {}", person.getUsername(), existingUser.isPresent());

        if (existingUser.isPresent()) {
            errors.rejectValue("username", "", "User with this username already exists");
        }
    }
}
