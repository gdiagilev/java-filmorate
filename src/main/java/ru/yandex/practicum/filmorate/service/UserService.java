package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public User create(User user) {
        return userStorage.add(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(int id) {
        return userStorage.getById(id)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public void addFriend(int userId, int friendId) {
        Optional<User> user = userStorage.getById(userId);
        Optional<User> friend = userStorage.getById(friendId);

        user.get().getFriends().add(friendId);
        friend.get().getFriends().add(userId);
    }

    public void removeFriend(int userId, int friendId) {
        Optional<User> user = userStorage.getById(userId);
        Optional<User> friend = userStorage.getById(friendId);

        user.get().getFriends().remove(friendId);
        friend.get().getFriends().remove(userId);
    }

    public List<Optional<User>> getFriends(int userId) {
        Optional<User> user = userStorage.getById(userId);

        return user.get().getFriends().stream()
                .map(userStorage::getById)
                .toList();
    }

    public List<Optional<User>> getCommonFriends(int userId, int otherId) {
        Optional<User> user = userStorage.getById(userId);
        Optional<User> other = userStorage.getById(otherId);

        return user.get().getFriends().stream()
                .filter(other.get().getFriends()::contains)
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }
}