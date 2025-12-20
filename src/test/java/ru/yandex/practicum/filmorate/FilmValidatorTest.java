package ru.yandex.practicum.filmorate;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.FilmValidator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidatorTest {

    @Test
    void shouldValidateCorrectFilm() {
        Film film = new Film();
        film.setId(1);
        film.setName("Test");
        film.setDescription("Normal description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertDoesNotThrow(() -> FilmValidator.validate(film));
    }

    @Test
    void shouldThrowIfNameIsEmpty() {
        Film film = new Film();
        film.setName("  ");
        film.setDuration(10);

        assertThrows(ValidationException.class,
                () -> FilmValidator.validate(film));
    }

    @Test
    void shouldThrowIfDescriptionTooLong() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("A".repeat(201)); // 201 символ
        film.setDuration(10);

        assertThrows(ValidationException.class,
                () -> FilmValidator.validate(film));
    }

    @Test
    void shouldThrowIfReleaseDateTooEarly() {
        Film film = new Film();
        film.setName("Film");
        film.setDuration(10);
        film.setReleaseDate(LocalDate.of(1800, 1, 1));

        assertThrows(ValidationException.class,
                () -> FilmValidator.validate(film));
    }

    @Test
    void shouldThrowIfDurationNotPositive() {
        Film film = new Film();
        film.setName("Film");
        film.setDuration(0);

        assertThrows(ValidationException.class,
                () -> FilmValidator.validate(film));
    }

    @Test
    void shouldAcceptFilmWithMinReleaseDate() {
        Film film = new Film();
        film.setName("Test");
        film.setDuration(10);
        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        assertDoesNotThrow(() -> FilmValidator.validate(film));
    }

    @Test
    void shouldAcceptDescriptionWith200Characters() {
        Film film = new Film();
        film.setName("Film");
        film.setDuration(10);
        film.setDescription("A".repeat(200));

        assertDoesNotThrow(() -> FilmValidator.validate(film));
    }
}