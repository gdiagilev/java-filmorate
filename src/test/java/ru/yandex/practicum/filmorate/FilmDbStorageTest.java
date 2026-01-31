package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    @Test
    void shouldAddFilm() {
        Film film = new Film();
        film.setName("Matrix");
        film.setDescription("Neo discovers reality");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);
        film.setMpa(new MpaRating(1, "G"));
        film.setGenres(List.of(new Genre(1, "Комедия")));

        Film saved = filmDbStorage.add(film);

        assertNotNull(saved.getId());
        assertEquals("Matrix", saved.getName());
        assertEquals(1, saved.getGenres().size());
        assertEquals("Комедия", saved.getGenres().get(0).getName());
    }

    @Test
    void shouldUpdateFilm() {
        Film film = new Film();
        film.setName("Matrix");
        film.setDescription("Neo discovers reality");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);
        film.setMpa(new MpaRating(1, "G"));
        film.setGenres(List.of(new Genre(1, "Комедия")));
        Film saved = filmDbStorage.add(film);

        saved.setName("Matrix Reloaded");
        saved.setDuration(138);
        saved.setGenres(List.of(new Genre(2, "Драма")));
        filmDbStorage.update(saved);

        Film updated = filmDbStorage.getById(saved.getId());
        assertEquals("Matrix Reloaded", updated.getName());
        assertEquals(138, updated.getDuration());
        assertEquals(1, updated.getGenres().size());
        assertEquals("Драма", updated.getGenres().get(0).getName());
    }

    @Test
    void shouldGetFilmById() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("Dream within a dream");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        film.setMpa(new MpaRating(2, "PG"));
        Film saved = filmDbStorage.add(film);

        Film found = filmDbStorage.getById(saved.getId());
        assertEquals("Inception", found.getName());
        assertEquals(148, found.getDuration());
    }

    @Test
    void shouldAddAndRemoveLike() {
        // --- создаём фильм ---
        Film film = new Film();
        film.setName("Matrix");
        film.setDescription("Neo discovers reality");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);
        film.setMpa(new MpaRating(1, "G"));
        Film savedFilm = filmDbStorage.add(film);

        // --- создаём пользователя прямо в тесте ---
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser = userDbStorage.add(user);

        // --- добавляем лайк ---
        filmDbStorage.addLike(savedFilm.getId(), savedUser.getId());

        // --- проверяем, что лайк добавился ---
        Film likedFilm = filmDbStorage.getById(savedFilm.getId());
        assertTrue(likedFilm.getLikes().contains(savedUser.getId()));

        // --- удаляем лайк ---
        filmDbStorage.removeLike(savedFilm.getId(), savedUser.getId());

        // --- проверяем, что лайк удалился ---
        Film unlikedFilm = filmDbStorage.getById(savedFilm.getId());
        assertFalse(unlikedFilm.getLikes().contains(savedUser.getId()));
    }
}