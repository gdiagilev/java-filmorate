package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDbStorage userStorage;

    // Создание пользователя
    public User create(User user) {
        validateUser(user);
        return userStorage.add(user);
    }

    // Обновление пользователя
    public User update(User user) {
        getById(user.getId());
        validateUser(user);
        return userStorage.update(user);
    }

    // Получение пользователя по ID
    public User getById(int id) {
        return userStorage.getById(id)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    // Получение всех пользователей
    public List<User> getAll() {
        return userStorage.getAll();
    }

    // Добавление друга
    public void addFriend(int userId, int friendId) {
        getById(userId);
        getById(friendId);
        try {
            userStorage.addFriend(userId, friendId);
        } catch (RuntimeException e) {
            throw new NotFoundException("Не удалось добавить друга");
        }
    }

    public void removeFriend(int userId, int friendId) {
        getById(userId);
        getById(friendId);
        try {
            userStorage.removeFriend(userId, friendId);
        } catch (RuntimeException e) {
            throw new NotFoundException("Дружбы не существует");
        }
    }




    // Получение друзей пользователя
    public List<User> getFriends(int userId) {
        getById(userId);
        return userStorage.getFriends(userId);
    }

    // Получение общих друзей двух пользователей
    public List<User> getCommonFriends(int userId, int otherId) {
        getById(userId);
        getById(otherId);
        List<User> friends1 = getFriends(userId);
        List<User> friends2 = getFriends(otherId);

        friends1.retainAll(friends2);
        return friends1;
    }

    // Валидация пользователя
    private void validateUser(User user) {
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }

        if (user.getLogin().contains(" ")) {
            throw new IllegalArgumentException("Логин не должен содержать пробелы");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата рождения не может быть в будущем");
        }
    }
}