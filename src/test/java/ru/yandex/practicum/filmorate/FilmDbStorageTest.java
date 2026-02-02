package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @Test
    void shouldAddFilm() {
        Film film = new Film();
        film.setName("Matrix");
        film.setDescription("Neo discovers reality");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        mpa.setName("G");
        film.setMpa(mpa);

        Genre genre = new Genre();
        genre.setId(1);
        genre.setName("Комедия");
        film.setGenres(Set.of(genre));

        Film saved = filmDbStorage.add(film);

        assertNotNull(saved.getId());
        assertEquals("Matrix", saved.getName());
        assertEquals(1, saved.getGenres().size());
    }

    @Test
    void shouldUpdateFilm() {
        Film film = new Film();
        film.setName("Matrix");
        film.setDescription("Neo discovers reality");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        mpa.setName("G");
        film.setMpa(mpa);

        Film saved = filmDbStorage.add(film);

        saved.setName("Matrix Reloaded");
        saved.setDuration(138);
        filmDbStorage.update(saved);

        Film updated = filmDbStorage.getById(saved.getId());
        assertEquals("Matrix Reloaded", updated.getName());
        assertEquals(138, updated.getDuration());
    }

    @Test
    void shouldGetFilmById() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("Dream within a dream");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        MpaRating mpa = new MpaRating();
        mpa.setId(2);
        mpa.setName("PG");
        film.setMpa(mpa);

        Film saved = filmDbStorage.add(film);

        Film found = filmDbStorage.getById(saved.getId());
        assertEquals("Inception", found.getName());
        assertEquals(148, found.getDuration());
    }

    @Test
    void shouldAddAndRemoveLike() {
        Film film = new Film();
        film.setName("Matrix");
        film.setDescription("Neo discovers reality");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        mpa.setName("G");
        film.setMpa(mpa);

        Film savedFilm = filmDbStorage.add(film);

        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("user");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User savedUser = userDbStorage.add(user);

        filmDbStorage.addLike(savedFilm.getId(), savedUser.getId());

        Integer likeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                savedFilm.getId(),
                savedUser.getId()
        );
        assertEquals(1, likeCount);

        filmDbStorage.removeLike(savedFilm.getId(), savedUser.getId());

        likeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?",
                Integer.class,
                savedFilm.getId(),
                savedUser.getId()
        );
        assertEquals(0, likeCount);
    }
}