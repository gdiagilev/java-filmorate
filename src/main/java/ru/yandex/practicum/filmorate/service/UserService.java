package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JdbcTemplate jdbcTemplate;
    private final FilmService filmService;
    private final UserDbStorage userStorage;
    private final EventService eventService;

    public User create(User user) {
        validateUser(user);
        return userStorage.add(user);
    }

    public User update(User user) {
        getById(user.getId());
        validateUser(user);
        return userStorage.update(user);
    }

    public User getById(int id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    private void validateUser(int userId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    public boolean addFriend(int userId, int friendId) {
        validateUser(userId);
        validateUser(friendId);

        boolean added = userStorage.addFriend(userId, friendId);
        if (added) {
            eventService.addEvent(userId, EventType.FRIEND, Operation.ADD, friendId);
        }
        return added;
    }

    public boolean removeFriend(int userId, int friendId) {
        validateUser(userId);
        validateUser(friendId);

        boolean removed = userStorage.removeFriend(userId, friendId);
        if (removed) {
            eventService.addEvent(userId, EventType.FRIEND, Operation.REMOVE, friendId);
        }
        return removed;
    }

    private void validateUserExists(int userId) {
        if (userId <= 0 || !userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    public List<User> getFriends(int userId) {
        validateUser(userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        validateUser(userId);
        validateUser(otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }

    public void deleteUser(int userId) {
        getById(userId);
        userStorage.delete(userId);
    }

    public List<Film> getRecommendations(int userId) {
        getById(userId);
        return filmService.getRecommendations(userId);
    }

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

    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public boolean userExists(int id) {
        try {
            return getById(id) != null;
        } catch (NotFoundException e) {
            return false;
        }
    }
}