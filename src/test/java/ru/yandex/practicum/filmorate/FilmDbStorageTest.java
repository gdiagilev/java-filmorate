package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Autowired
    private UserDbStorage userDbStorage;

    private Film testFilm;
    private Film addedFilm;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setEmail("user1@mail.ru");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        user1 = userDbStorage.add(user1);

        user2 = new User();
        user2.setEmail("user2@mail.ru");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1991, 2, 2));
        user2 = userDbStorage.add(user2);

        testFilm = new Film();
        testFilm.setName("Тестовый фильм");
        testFilm.setDescription("Описание тестового фильма");
        testFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        testFilm.setDuration(120);
        testFilm.setMpa(MpaRating.PG_13);
        testFilm.setGenres(Set.of(
                new Genre(1, "Комедия"),
                new Genre(2, "Драма")
        ));

        addedFilm = filmDbStorage.add(testFilm);

        filmDbStorage.addLike(addedFilm.getId(), user1.getId());
        filmDbStorage.addLike(addedFilm.getId(), user2.getId());
    }

    @Test
    void testAddAndGetFilm() {
        Film fetched = filmDbStorage.getById(addedFilm.getId()).orElseThrow();
        assertEquals("Тестовый фильм", fetched.getName());
        assertEquals(2, fetched.getGenres().size());
        assertEquals(MpaRating.PG_13, fetched.getMpa());
    }

    @Test
    void testUpdateFilm() {
        addedFilm.setName("Обновлённый фильм");
        addedFilm.setGenres(Set.of(new Genre(3, "Боевик")));
        filmDbStorage.update(addedFilm);

        Film updated = filmDbStorage.getById(addedFilm.getId()).orElseThrow();
        assertEquals("Обновлённый фильм", updated.getName());
        assertEquals(1, updated.getGenres().size());
        assertTrue(updated.getGenres().stream().anyMatch(g -> g.getId() == 3));
    }

    @Test
    void testAddAndRemoveLike() {
        List<Film> topFilms = filmDbStorage.getTopLikedFilms(10);
        assertEquals(1, topFilms.size());
        assertEquals(addedFilm.getId(), topFilms.get(0).getId());

        filmDbStorage.removeLike(addedFilm.getId(), user1.getId());
        topFilms = filmDbStorage.getTopLikedFilms(10);
        assertEquals(1, topFilms.size());
    }

    @Test
    void testGetByIdNotFound() {
        assertThrows(NotFoundException.class, () -> filmDbStorage.getById(999).orElseThrow(
                () -> new NotFoundException("Фильм не найден")
        ));
    }
}