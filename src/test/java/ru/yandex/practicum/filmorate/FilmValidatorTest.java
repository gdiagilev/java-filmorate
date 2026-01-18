package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldValidateCorrectFilm() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("Normal description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailIfNameIsBlank() {
        Film film = new Film();
        film.setName("   ");
        film.setDuration(10);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailIfDescriptionTooLong() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("A".repeat(201));
        film.setDuration(10);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailIfReleaseDateTooEarly() {
        Film film = new Film();
        film.setName("Film");
        film.setDuration(10);
        film.setReleaseDate(LocalDate.of(1800, 1, 1));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldFailIfDurationNotPositive() {
        Film film = new Film();
        film.setName("Film");
        film.setDuration(0);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldAcceptFilmWithMinReleaseDate() {
        Film film = new Film();
        film.setName("Test");
        film.setDuration(10);
        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldAcceptDescriptionWith200Characters() {
        Film film = new Film();
        film.setName("Film");
        film.setDuration(10);
        film.setDescription("A".repeat(200));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertTrue(violations.isEmpty());
    }
}