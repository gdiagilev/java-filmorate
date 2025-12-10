package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerEmptyRequestTest {

    @Test
    void shouldThrowOnNullFilm() {
        FilmController controller = new FilmController();

        assertThrows(ValidationException.class, () -> controller.addFilm(null));
    }
}