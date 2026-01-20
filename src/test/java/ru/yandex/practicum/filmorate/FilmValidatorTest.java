package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.time.LocalDate;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidatorTest {

    private static Film validFilm;

    @BeforeAll
    static void setUp() {
        validFilm = new Film();
        validFilm.setName("Test Film");
        validFilm.setDescription("Normal description");
        validFilm.setDuration(120);
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setMpa(MpaRating.PG); // Пример
        validFilm.setGenres(new HashSet<>());
    }

    @Test
    void shouldValidateCorrectFilm() {
        assertDoesNotThrow(() -> FilmValidator.validate(validFilm));
    }

    @Test
    void shouldFailIfNameIsBlank() {
        validFilm.setName("   ");
        assertThrows(Exception.class, () -> FilmValidator.validate(validFilm));
        validFilm.setName("Test Film"); // вернуть корректное значение
    }

    @Test
    void shouldFailIfDescriptionTooLong() {
        validFilm.setDescription("A".repeat(201));
        assertThrows(Exception.class, () -> FilmValidator.validate(validFilm));
        validFilm.setDescription("Normal description");
    }

    @Test
    void shouldFailIfReleaseDateTooEarly() {
        validFilm.setReleaseDate(LocalDate.of(1800, 1, 1));
        assertThrows(Exception.class, () -> FilmValidator.validate(validFilm));
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
    }

    @Test
    void shouldFailIfDurationNotPositive() {
        validFilm.setDuration(0);
        assertThrows(Exception.class, () -> FilmValidator.validate(validFilm));
        validFilm.setDuration(120);
    }

    @Test
    void shouldFailIfMpaIsNull() {
        validFilm.setMpa(null);
        assertThrows(Exception.class, () -> FilmValidator.validate(validFilm));
        validFilm.setMpa(MpaRating.PG);
    }

    @Test
    void shouldFailIfGenresContainNull() {
        validFilm.getGenres().add(null);
        assertThrows(Exception.class, () -> FilmValidator.validate(validFilm));
        validFilm.setGenres(new HashSet<>());
    }
}