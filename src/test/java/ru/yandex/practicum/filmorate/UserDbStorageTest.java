package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @Test
    void shouldAddUser() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("login");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User saved = userDbStorage.add(user);

        assertNotNull(saved.getId());
        assertEquals("user@mail.ru", saved.getEmail());
        assertEquals("login", saved.getLogin());
        assertEquals("User Name", saved.getName());
    }

    @Test
    void shouldSetLoginAsNameIfNameIsBlank() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("login");
        user.setName("   ");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User saved = userDbStorage.add(user);

        assertEquals("login", saved.getName());
    }

    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("login");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User saved = userDbStorage.add(user);

        saved.setName("Updated Name");
        saved.setEmail("updated@mail.ru");
        userDbStorage.update(saved);

        Optional<User> updated = userDbStorage.getById(saved.getId());
        assertTrue(updated.isPresent());
        assertEquals("Updated Name", updated.get().getName());
        assertEquals("updated@mail.ru", updated.get().getEmail());
    }

    @Test
    void shouldGetUserById() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("login");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User saved = userDbStorage.add(user);

        Optional<User> found = userDbStorage.getById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void shouldGetAllUsers() {
        User user1 = new User();
        user1.setEmail("user1@mail.ru");
        user1.setLogin("login1");
        user1.setName("User1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("login2");
        user2.setName("User2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));

        userDbStorage.add(user1);
        userDbStorage.add(user2);

        List<User> users = userDbStorage.getAll();

        assertEquals(2, users.size());
    }

    @Test
    void shouldAddAndRemoveFriend() {
        User user = createUser("user@mail.ru", "user");
        User friend = createUser("friend@mail.ru", "friend");

        userDbStorage.addFriend(user.getId(), friend.getId());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?",
                Integer.class,
                user.getId(),
                friend.getId()
        );
        assertEquals(1, count);

        userDbStorage.removeFriend(user.getId(), friend.getId());

        count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?",
                Integer.class,
                user.getId(),
                friend.getId()
        );
        assertEquals(0, count);
    }

    @Test
    void shouldGetFriends() {
        User user = createUser("user@mail.ru", "user");
        User friend = createUser("friend@mail.ru", "friend");

        userDbStorage.addFriend(user.getId(), friend.getId());

        List<User> friends = userDbStorage.getFriends(user.getId());

        assertEquals(1, friends.size());
        assertEquals(friend.getId(), friends.get(0).getId());
    }

    @Test
    void shouldGetCommonFriends() {
        User user1 = createUser("user1@mail.ru", "user1");
        User user2 = createUser("user2@mail.ru", "user2");
        User common = createUser("common@mail.ru", "common");

        userDbStorage.addFriend(user1.getId(), common.getId());
        userDbStorage.addFriend(user2.getId(), common.getId());

        List<User> commonFriends = userDbStorage.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(common.getId(), commonFriends.get(0).getId());
    }

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userDbStorage.add(user);
    }
}