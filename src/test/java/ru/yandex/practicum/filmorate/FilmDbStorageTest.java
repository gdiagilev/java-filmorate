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

        Integer likeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?", Integer.class, savedFilm.getId(), savedUser.getId());
        assertEquals(1, likeCount);

        filmDbStorage.removeLike(savedFilm.getId(), savedUser.getId());

        likeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?", Integer.class, savedFilm.getId(), savedUser.getId());
        assertEquals(0, likeCount);
    }

    @Test
    void shouldSearchFilmsByTitle() {
        Film film1 = createTestFilm("Matrix", "Sci-Fi");
        Film film2 = createTestFilm("Matrix Reloaded", "Sequel");
        filmDbStorage.add(film1);
        filmDbStorage.add(film2);

        List<Film> found = filmDbStorage.search("Matrix", List.of("title"));
        assertEquals(2, found.size());
    }

    @Test
    void shouldSearchFilmsByDirector() {
        // Создаём режиссёра
        Director director = new Director();
        director.setName("Christopher Nolan");
        directorDbStorage.add(director); // предположим, что поле directorDbStorage добавлено в тестовый класс

        Film film = createTestFilm("Inception", "Dream within a dream");
        film = filmDbStorage.add(film);
        // Связываем фильм с режиссёром
        linkFilmWithDirector(film.getId(), director.getId());

        List<Film> found = filmDbStorage.search("nolan", List.of("director"));
        assertEquals(1, found.size());
        assertEquals("Inception", found.get(0).getName());
    }

    @Test
    void shouldSearchFilmsByTitleAndDirector() {
        // Фильм с режиссёром
        Director director = new Director();
        director.setName("Quentin Tarantino");
        directorDbStorage.add(director);
        Film film1 = createTestFilm("Pulp Fiction", "Crime");
        film1 = filmDbStorage.add(film1);
        linkFilmWithDirector(film1.getId(), director.getId());

        // Фильм по названию
        Film film2 = createTestFilm("Fiction Story", "Drama");
        filmDbStorage.add(film2);

        List<Film> found = filmDbStorage.search("fiction", List.of("title", "director"));
        assertEquals(2, found.size());
    }

    @Test
    void shouldReturnEmptyListWhenNothingFound() {
        List<Film> found = filmDbStorage.search("nonexistent", List.of("title"));
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldSortSearchResultsByLikes() {
        // Создаём два фильма
        Film popular = createTestFilm("Popular Film", "Description");
        Film lessPopular = createTestFilm("Less Popular", "Description");
        filmDbStorage.add(popular);
        filmDbStorage.add(lessPopular);

        // Создаём пользователей
        User user1 = createTestUser("user1@mail.ru", "user1");
        User user2 = createTestUser("user2@mail.ru", "user2");
        userDbStorage.add(user1);
        userDbStorage.add(user2);

        // Лайки: популярный получает 2 лайка, менее популярный — 1
        filmDbStorage.addLike(popular.getId(), user1.getId());
        filmDbStorage.addLike(popular.getId(), user2.getId());
        filmDbStorage.addLike(lessPopular.getId(), user1.getId());

        List<Film> found = filmDbStorage.search("Popular", List.of("title"));
        assertEquals(2, found.size());
        assertEquals(popular.getId(), found.get(0).getId()); // самый популярный первым
        assertEquals(lessPopular.getId(), found.get(1).getId());
    }

    @Test
    void shouldGetFilmsByDirectorSortedByYear() {
        Director director = new Director();
        director.setName("James Cameron");
        directorDbStorage.add(director);

        Film film1 = createTestFilm("Avatar", "Sci-Fi");
        film1.setReleaseDate(LocalDate.of(2009, 12, 18));
        film1 = filmDbStorage.add(film1);
        linkFilmWithDirector(film1.getId(), director.getId());

        Film film2 = createTestFilm("Titanic", "Drama");
        film2.setReleaseDate(LocalDate.of(1997, 12, 19));
        film2 = filmDbStorage.add(film2);
        linkFilmWithDirector(film2.getId(), director.getId());

        List<Film> films = filmDbStorage.getFilmsByDirector(director.getId(), "year");
        assertEquals(2, films.size());
        // Проверяем сортировку по году (от старых к новым)
        assertEquals(film2.getId(), films.get(0).getId()); // Titanic (1997)
        assertEquals(film1.getId(), films.get(1).getId()); // Avatar (2009)
    }

    @Test
    void shouldGetFilmsByDirectorSortedByLikes() {
        Director director = new Director();
        director.setName("Christopher Nolan");
        directorDbStorage.add(director);

        Film film1 = createTestFilm("Inception", "Dream");
        film1 = filmDbStorage.add(film1);
        linkFilmWithDirector(film1.getId(), director.getId());

        Film film2 = createTestFilm("Interstellar", "Space");
        film2 = filmDbStorage.add(film2);
        linkFilmWithDirector(film2.getId(), director.getId());

        // Создаём пользователей и лайки
        User user1 = createTestUser("user1@mail.ru", "user1");
        User user2 = createTestUser("user2@mail.ru", "user2");
        userDbStorage.add(user1);
        userDbStorage.add(user2);

        filmDbStorage.addLike(film1.getId(), user1.getId());
        filmDbStorage.addLike(film1.getId(), user2.getId()); // film1 - 2 лайка
        filmDbStorage.addLike(film2.getId(), user1.getId()); // film2 - 1 лайк

        List<Film> films = filmDbStorage.getFilmsByDirector(director.getId(), "likes");
        assertEquals(2, films.size());
        assertEquals(film1.getId(), films.get(0).getId()); // более популярный первым
        assertEquals(film2.getId(), films.get(1).getId());
    }

    @Test
    void shouldReturnEmptyRecommendationsForUserWithNoLikes() {
        User user = createTestUser("user@mail.ru", "user");
        userDbStorage.add(user);

        List<Film> recommendations = filmDbStorage.getRecommendations(user.getId());
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void shouldReturnEmptyRecommendationsWhenNoSimilarUsers() {
        User user = createTestUser("user@mail.ru", "user");
        userDbStorage.add(user);

        Film film = createTestFilm("Film", "Desc");
        film = filmDbStorage.add(film);
        filmDbStorage.addLike(film.getId(), user.getId());

        List<Film> recommendations = filmDbStorage.getRecommendations(user.getId());
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void shouldReturnRecommendationsForUserWithSimilarLikes() {
        // Создаём пользователей
        User user1 = createTestUser("user1@mail.ru", "user1");
        User user2 = createTestUser("user2@mail.ru", "user2");
        userDbStorage.add(user1);
        userDbStorage.add(user2);

        // Создаём фильмы
        Film film1 = createTestFilm("Film1", "Desc1");
        Film film2 = createTestFilm("Film2", "Desc2");
        Film film3 = createTestFilm("Film3", "Desc3");
        filmDbStorage.add(film1);
        filmDbStorage.add(film2);
        filmDbStorage.add(film3);

        // user1 лайкает film1
        filmDbStorage.addLike(film1.getId(), user1.getId());

        // user2 лайкает film1 и film2
        filmDbStorage.addLike(film1.getId(), user2.getId());
        filmDbStorage.addLike(film2.getId(), user2.getId());

        // user3 лайкает film3 (не должен влиять)
        User user3 = createTestUser("user3@mail.ru", "user3");
        userDbStorage.add(user3);
        filmDbStorage.addLike(film3.getId(), user3.getId());

        // Рекомендации для user1 должны содержать film2
        List<Film> recommendations = filmDbStorage.getRecommendations(user1.getId());
        assertEquals(1, recommendations.size());
        assertEquals(film2.getId(), recommendations.get(0).getId());

        // Рекомендации для user2 должны быть пусты (он уже лайкнул всё, что есть у user1)
        recommendations = filmDbStorage.getRecommendations(user2.getId());
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void shouldReturnRecommendationsSortedByScore() {
        // Сценарий: user1 лайкает film1
        // user2 лайкает film1 и film2
        // user3 лайкает film1, film2 и film3
        // Тогда для user1 рекомендуются film2 (2 совпадения) и film3 (1 совпадение) с сортировкой по убыванию

        User user1 = createTestUser("user1@mail.ru", "user1");
        User user2 = createTestUser("user2@mail.ru", "user2");
        User user3 = createTestUser("user3@mail.ru", "user3");
        userDbStorage.add(user1);
        userDbStorage.add(user2);
        userDbStorage.add(user3);

        Film film1 = createTestFilm("Film1", "Desc1");
        Film film2 = createTestFilm("Film2", "Desc2");
        Film film3 = createTestFilm("Film3", "Desc3");
        filmDbStorage.add(film1);
        filmDbStorage.add(film2);
        filmDbStorage.add(film3);

        filmDbStorage.addLike(film1.getId(), user1.getId());

        filmDbStorage.addLike(film1.getId(), user2.getId());
        filmDbStorage.addLike(film2.getId(), user2.getId());

        filmDbStorage.addLike(film1.getId(), user3.getId());
        filmDbStorage.addLike(film2.getId(), user3.getId());
        filmDbStorage.addLike(film3.getId(), user3.getId());

        List<Film> recommendations = filmDbStorage.getRecommendations(user1.getId());
        assertEquals(2, recommendations.size());
        assertEquals(film2.getId(), recommendations.get(0).getId()); // film2 (2 совпадения)
        assertEquals(film3.getId(), recommendations.get(1).getId()); // film3 (1 совпадение)
    }

    private Film createTestFilm(String name, String description) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);
        return film;
    }

    private User createTestUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    private void linkFilmWithDirector(int filmId, int directorId) {
        jdbcTemplate.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)", filmId, directorId);
    }
}