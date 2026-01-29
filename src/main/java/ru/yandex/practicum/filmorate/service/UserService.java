package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDbStorage userStorage;

    public User create(User user) {
        return userStorage.add(user);
    }

    public User update(User user) {
        getById(user.getId());
        return userStorage.update(user);
    }

    public User getById(int id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public void addFriend(int userId, int friendId) {
        getById(userId);
        getById(friendId);
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        getById(userId);
        getById(friendId);
        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(int userId) {
        getById(userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        List<Integer> friends1 = userStorage.getFriends(userId)
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());

        List<Integer> friends2 = userStorage.getFriends(otherId)
                .stream()
                .map(User::getId)
                .collect(Collectors.toList());

        friends1.retainAll(friends2);

        return friends1.stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }
}