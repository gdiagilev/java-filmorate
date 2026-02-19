package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film add(Film film) {
        removeDuplicateGenres(film);

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Ошибка при получении сгенерированного ID для фильма");
        }
        film.setId(key.intValue());

        saveGenres(film);

        return film;
    }

    @Override
    public Film update(Film film) {
        getById(film.getId());
        removeDuplicateGenres(film);

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        updateFilmGenres(film);
        return film;
    }

    @Override
    public Film getById(int id) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa m ON f.mpa_id = m.id " +
                "WHERE f.id = ?";

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film f = new Film();
            f.setId(rs.getInt("id"));
            f.setName(rs.getString("name"));
            f.setDescription(rs.getString("description"));
            f.setReleaseDate(rs.getDate("release_date").toLocalDate());
            f.setDuration(rs.getInt("duration"));

            MpaRating mpa = new MpaRating();
            mpa.setId(rs.getInt("mpa_id"));
            mpa.setName(rs.getString("mpa_name"));
            f.setMpa(mpa);

            f.setGenres(getGenresByFilmId(f.getId()));
            return f;
        }, id);

        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }

        return films.get(0);
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                "FROM films f JOIN mpa m ON f.mpa_id = m.id";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film f = new Film();
            f.setId(rs.getInt("id"));
            f.setName(rs.getString("name"));
            f.setDescription(rs.getString("description"));
            f.setReleaseDate(rs.getDate("release_date").toLocalDate());
            f.setDuration(rs.getInt("duration"));

            MpaRating mpa = new MpaRating();
            mpa.setId(rs.getInt("mpa_id"));
            mpa.setName(rs.getString("mpa_name"));
            f.setMpa(mpa);

            f.setGenres(getGenresByFilmId(f.getId()));
            return f;
        });
    }

    @Override
    public void addLike(int filmId, int userId) {
        getById(filmId);
        String sql = "MERGE INTO film_likes (film_id, user_id) KEY(film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        getById(filmId);
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getTopLikedFilms(int count) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name, " +
                "COUNT(l.user_id) AS like_count " +
                "FROM films f " +
                "JOIN mpa m ON f.mpa_id = m.id " +
                "LEFT JOIN film_likes l ON f.id = l.film_id " +
                "GROUP BY f.id, m.name " +
                "ORDER BY like_count DESC " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film f = new Film();
            f.setId(rs.getInt("id"));
            f.setName(rs.getString("name"));
            f.setDescription(rs.getString("description"));
            f.setReleaseDate(rs.getDate("release_date").toLocalDate());
            f.setDuration(rs.getInt("duration"));

            MpaRating mpa = new MpaRating();
            mpa.setId(rs.getInt("mpa_id"));
            mpa.setName(rs.getString("mpa_name"));
            f.setMpa(mpa);

            f.setGenres(getGenresByFilmId(f.getId()));
            return f;
        }, count);
    }

    private void updateFilmGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);
    }

    private void saveGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> added = new HashSet<>();
            for (Genre genre : film.getGenres()) {
                if (added.add(genre.getId())) {
                    jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                            film.getId(), genre.getId());
                }
            }
        }
    }

    private Set<Genre> getGenresByFilmId(int filmId) {
        String sql = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.id";
        return new HashSet<>(
                jdbcTemplate.query(sql, (rs, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(rs.getInt("id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                }, filmId)
        );
    }

    private void removeDuplicateGenres(Film film) {
        if (film.getGenres() != null) {
            Set<Integer> uniqueIds = new HashSet<>();
            Set<Genre> uniqueGenres = new LinkedHashSet<>();
            for (Genre genre : film.getGenres()) {
                if (uniqueIds.add(genre.getId())) {
                    uniqueGenres.add(genre);
                }
            }
            film.setGenres(uniqueGenres);
        }
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        return film;
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        String sql = """
                SELECT f.*
                FROM films f
                JOIN film_likes l1 ON f.id = l1.film_id
                JOIN film_likes l2 ON f.id = l2.film_id
                WHERE l1.user_id = ?
                  AND l2.user_id = ?
                ORDER BY (
                    SELECT COUNT(*)
                    FROM film_likes fl
                    WHERE fl.film_id = f.id
                ) DESC
                """;

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, userId, friendId);
        films.forEach(film -> film.setGenres(getGenresByFilmId(film.getId())));
        return films;
    }

    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {

        StringBuilder sql = new StringBuilder("""
                SELECT f.*, COUNT(fl.user_id) AS likes
                FROM films f
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                """);

        if (genreId != null) {
            sql.append(" JOIN film_genres fg ON f.id = fg.film_id ");
        }

        sql.append(" WHERE 1=1 ");

        if (genreId != null) {
            sql.append(" AND fg.genre_id = ").append(genreId);
        }

        if (year != null) {
            sql.append(" AND EXTRACT(YEAR FROM f.release_date) = ").append(year);
        }

        sql.append("""
                GROUP BY f.id
                ORDER BY likes DESC
                LIMIT ?
                """);

        List<Film> films = jdbcTemplate.query(sql.toString(), this::mapRowToFilm, count);

        films.forEach(film -> film.setGenres(getGenresByFilmId(film.getId())));

        return films;
    }

    @Override
    public void delete(int filmId) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, filmId);
    }
}