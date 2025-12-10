package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final List<User> users = new ArrayList<>();
    private int nextUserId = 1;

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);

        if (user == null) {
            log.warn("Ошибка: пустое тело запроса при создании пользователя");
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        UserValidator.validate(user);

        user.setId(nextUserId++);
        users.add(user);

        log.info("Пользователь успешно создан: id={}", user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("Получен запрос на обновление пользователя: {}", user);

        if (user == null) {
            log.warn("Ошибка: пустое тело запроса при обновлении пользователя");
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        UserValidator.validate(user);

        Optional<User> existing = users.stream()
                .filter(u -> u.getId() == user.getId())
                .findFirst();

        if (existing.isEmpty()) {
            log.warn("Пользователь с id={} не найден", user.getId());
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }

        users.remove(existing.get());
        users.add(user);

        log.info("Пользователь успешно обновлён: id={}", user.getId());
        return user;
    }

    @GetMapping
    public List<User> getAllUsers() {
        log.info("Получен запрос на получение всех пользователей ({} шт.)", users.size());
        return users;
    }
}