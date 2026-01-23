package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerEmptyRequestTest {

    @Test
    void shouldThrowOnNullUser() {
        UserService stubService = new UserService(null); // stub, если не нужен storage
        UserController controller = new UserController(stubService);

        assertThrows(ValidationException.class, () -> controller.create(null));
    }
}