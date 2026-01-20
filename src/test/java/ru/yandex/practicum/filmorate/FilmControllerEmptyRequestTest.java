package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.service.FilmService;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerEmptyRequestTest {

    @Test
    void shouldThrowOnNullFilm() {
        FilmService stubService = new FilmService(null, null);

        FilmController controller = new FilmController(stubService);

        // При прямом вызове метод будет работать с null, но @Valid не сработает без Spring
        assertThrows(NullPointerException.class, () -> controller.add(null));
    }
}