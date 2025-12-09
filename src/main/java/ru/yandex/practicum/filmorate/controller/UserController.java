package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.validation.UserValidator;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final List<User> users = new ArrayList<>();

    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);
        UserValidator.validate(user);
        users.add(user);
        log.info("Пользователь успешно создан: id={}", user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        log.info("Получен запрос на обновление пользователя: {}", user);
        UserValidator.validate(user);
        users.removeIf(u -> u.getId() == user.getId());
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
