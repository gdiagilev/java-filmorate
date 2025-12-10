package ru.yandex.practicum.filmorate;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserValidatorTest {

    @Test
    void shouldValidateCorrectUser() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@mail.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertDoesNotThrow(() -> UserValidator.validate(user));
    }

    @Test
    void shouldThrowIfEmailInvalid() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("login");

        assertThrows(ValidationException.class,
                () -> UserValidator.validate(user));
    }

    @Test
    void shouldThrowIfLoginEmpty() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("   ");

        assertThrows(ValidationException.class,
                () -> UserValidator.validate(user));
    }

    @Test
    void shouldThrowIfLoginContainsSpaces() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("bad login");

        assertThrows(ValidationException.class,
                () -> UserValidator.validate(user));
    }

    @Test
    void shouldThrowIfBirthdayInFuture() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class,
                () -> UserValidator.validate(user));
    }

    @Test
    void shouldSetNameToLoginIfNameIsBlank() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("login");
        user.setName("   "); // пустое имя

        UserValidator.validate(user);

        assertEquals("login", user.getName());
    }
}