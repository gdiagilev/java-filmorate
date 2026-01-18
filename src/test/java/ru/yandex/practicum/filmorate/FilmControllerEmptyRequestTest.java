package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerEmptyRequestTest {

    @Test
    void shouldThrowOnNullFilm() {
        FilmService stubService = new FilmService(null, null) {
            @Override
            public Film create(Film film) {
                throw new IllegalStateException("Не должен вызываться");
            }
        };

        FilmController controller = new FilmController(stubService);

        assertThrows(ValidationException.class, () -> controller.add(null));
    }
}