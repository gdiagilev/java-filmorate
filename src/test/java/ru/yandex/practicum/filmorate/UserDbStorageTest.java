package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class UserDbStorageTest {

    @Autowired
    private UserDbStorage userDbStorage;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setup() {
        user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        user3 = new User();
        user3.setEmail("user3@example.com");
        user3.setLogin("user3");
        user3.setName("User Three");
        user3.setBirthday(LocalDate.of(1992, 3, 3));

        user1 = userDbStorage.add(user1);
        user2 = userDbStorage.add(user2);
        user3 = userDbStorage.add(user3);
    }

    @Test
    void testAddAndGetUser() {
        User fetched = userDbStorage.getById(user1.getId()).orElse(null);
        assertNotNull(fetched);
        assertEquals("user1@example.com", fetched.getEmail());
    }

    @Test
    void testUpdateUser() {
        user1.setName("Updated Name");
        userDbStorage.update(user1);

        User fetched = userDbStorage.getById(user1.getId()).orElse(null);
        assertNotNull(fetched);
        assertEquals("Updated Name", fetched.getName());
    }

    @Test
    void testGetAllUsers() {
        List<User> all = userDbStorage.getAll();
        assertEquals(3, all.size());
    }

    @Test
    void testAddAndGetFriends() {
        userDbStorage.addFriend(user1.getId(), user2.getId());
        userDbStorage.addFriend(user1.getId(), user3.getId());

        List<User> friends = userDbStorage.getFriends(user1.getId());
        assertEquals(2, friends.size());
        assertTrue(friends.stream().anyMatch(u -> u.getId() == user2.getId()));
        assertTrue(friends.stream().anyMatch(u -> u.getId() == user3.getId()));
    }

    @Test
    void testRemoveFriend() {
        userDbStorage.addFriend(user1.getId(), user2.getId());
        userDbStorage.addFriend(user1.getId(), user3.getId());

        userDbStorage.removeFriend(user1.getId(), user2.getId());
        List<User> friends = userDbStorage.getFriends(user1.getId());
        assertEquals(1, friends.size());
        assertEquals(user3.getId(), friends.get(0).getId());
    }

    @Test
    void testCommonFriends() {
        userDbStorage.addFriend(user1.getId(), user3.getId());
        userDbStorage.addFriend(user2.getId(), user3.getId());

        List<User> friendsOfUser1 = userDbStorage.getFriends(user1.getId());
        List<User> friendsOfUser2 = userDbStorage.getFriends(user2.getId());

        long commonCount = friendsOfUser1.stream()
                .filter(u -> friendsOfUser2.stream().anyMatch(u2 -> u2.getId() == u.getId()))
                .count();

        assertEquals(1, commonCount);
        assertEquals(user3.getId(), friendsOfUser1.stream()
                .filter(u -> friendsOfUser2.stream().anyMatch(u2 -> u2.getId() == u.getId()))
                .findFirst().get().getId());
    }
}