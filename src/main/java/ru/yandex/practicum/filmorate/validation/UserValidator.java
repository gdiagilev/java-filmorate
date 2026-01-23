package ru.yandex.practicum.filmorate.validation;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@Slf4j
public class UserValidator {

    public static void validate(User user) {
        if (user == null) {
            throw new ValidationException("Пользователь не может быть null");
        }

        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Ошибка валидации пользователя: некорректный email '{}'", user.getEmail());
            throw new ValidationException("Некорректный email: должен содержать '@' и не быть пустым.");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Ошибка валидации пользователя: некорректный логин '{}'", user.getLogin());
            throw new ValidationException("Логин не может быть пустым или содержать пробелы.");
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации пользователя: будущая дата рождения {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Имя пользователя не указано — используется логин '{}'", user.getLogin());
            user.setName(user.getLogin());
        }
    }
}