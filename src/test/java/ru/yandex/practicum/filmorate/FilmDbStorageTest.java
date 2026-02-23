
package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;
    private final DirectorDbStorage directorDbStorage;

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