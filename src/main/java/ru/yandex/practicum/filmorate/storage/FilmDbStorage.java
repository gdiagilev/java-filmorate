package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
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
        saveDirectors(film);

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
        updateFilmDirectors(film);

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
            f.setDirectors(getDirectorsByFilmId(f.getId()));

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
            f.setDirectors(getDirectorsByFilmId(f.getId()));

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
            f.setDirectors(getDirectorsByFilmId(f.getId()));

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
    private Set<Director> getDirectorsByFilmId(int filmId) {
        String sql = "SELECT d.id, d.name FROM directors d " +
                "JOIN film_directors fd ON d.id = fd.director_id " +
                "WHERE fd.film_id = ? ORDER BY d.id";
        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> {
            Director director = new Director();
            director.setId(rs.getInt("id"));
            director.setName(rs.getString("name"));
            return director;
        }, filmId));
    }

    private void saveDirectors(Film film) {
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            Set<Integer> added = new HashSet<>();
            for (Director director : film.getDirectors()) {
                if (added.add(director.getId())) {
                    jdbcTemplate.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)",
                            film.getId(), director.getId());
                }
            }
        }
    }

    private void updateFilmDirectors(Film film) {
        jdbcTemplate.update("DELETE FROM film_directors WHERE film_id = ?", film.getId());
        saveDirectors(film);
    }

    @Override
    public List<Film> search(String query, List<String> fields) {
        StringBuilder whereClause = new StringBuilder();
        List<Object> params = new ArrayList<>();

        boolean searchByTitle = fields.contains("title");
        boolean searchByDirector = fields.contains("director");

        if (searchByTitle && searchByDirector) {
            whereClause.append("(LOWER(f.name) LIKE LOWER(?) OR LOWER(d.name) LIKE LOWER(?))");
            params.add("%" + query + "%");
            params.add("%" + query + "%");
        } else if (searchByTitle) {
            whereClause.append("LOWER(f.name) LIKE LOWER(?)");
            params.add("%" + query + "%");
        } else if (searchByDirector) {
            whereClause.append("LOWER(d.name) LIKE LOWER(?)");
            params.add("%" + query + "%");
        } else {
            whereClause.append("LOWER(f.name) LIKE LOWER(?)");
            params.add("%" + query + "%");
        }

        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name, " +
                "(SELECT COUNT(*) FROM film_likes l WHERE l.film_id = f.id) AS like_count " +
                "FROM films f " +
                "JOIN mpa m ON f.mpa_id = m.id " +
                "LEFT JOIN film_directors fd ON f.id = fd.film_id " +
                "LEFT JOIN directors d ON fd.director_id = d.id " +
                "WHERE " + whereClause + " " +
                "GROUP BY f.id, m.name " +
                "ORDER BY like_count DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = mapFilm(rs);
            film.setGenres(getGenresByFilmId(film.getId()));
            film.setDirectors(getDirectorsByFilmId(film.getId()));
            return film;
        }, params.toArray());
    }

    @Override
    public List<Film> getRecommendations(int userId) {
        // Проверяем, есть ли у пользователя лайки
        Integer likesCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE user_id = ?",
                Integer.class, userId);
        if (likesCount == null || likesCount == 0) {
            return Collections.emptyList();
        }

        // Основной запрос с использованием CTE (работает в H2)
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name\n" +
                "FROM films f\n" +
                "JOIN mpa m ON f.mpa_id = m.id\n" +
                "JOIN film_likes l ON f.id = l.film_id\n" +
                "WHERE l.user_id IN (\n" +
                "    SELECT DISTINCT l2.user_id\n" +
                "    FROM film_likes l1\n" +
                "    JOIN film_likes l2 ON l1.film_id = l2.film_id\n" +
                "    WHERE l1.user_id = ? AND l2.user_id != ?\n" +
                ")\n" +
                "AND f.id NOT IN (SELECT film_id FROM film_likes WHERE user_id = ?)\n" +
                "GROUP BY f.id, m.name\n" +
                "ORDER BY COUNT(l.user_id) DESC\n" +
                "LIMIT 10;";

        List<Film> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = mapFilm(rs);
            film.setGenres(getGenresByFilmId(film.getId()));
            film.setDirectors(getDirectorsByFilmId(film.getId()));
            return film;
        }, userId, userId, userId); // userId передаётся трижды: для user_likes, similar_users и исключения


        return result;
    }

    private Film mapFilm(ResultSet rs) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        MpaRating mpa = new MpaRating();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));
        film.setMpa(mpa);

        // Жанры и режиссёры загружаются отдельными запросами, поэтому здесь не заполняем
        return film;
    }

    @Override
    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        // Определяем сортировку
        String orderBy;
        if ("year".equalsIgnoreCase(sortBy)) {
            orderBy = "f.release_date";
        } else if ("likes".equalsIgnoreCase(sortBy)) {
            orderBy = "like_count DESC";
        } else {
            // По умолчанию сортируем по году
            orderBy = "f.release_date";
        }

        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name, " +
                "(SELECT COUNT(*) FROM film_likes l WHERE l.film_id = f.id) AS like_count " +
                "FROM films f " +
                "JOIN mpa m ON f.mpa_id = m.id " +
                "JOIN film_directors fd ON f.id = fd.film_id " +
                "WHERE fd.director_id = ? " +
                "ORDER BY " + orderBy;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = mapFilm(rs);
            film.setGenres(getGenresByFilmId(film.getId()));
            film.setDirectors(getDirectorsByFilmId(film.getId()));
            return film;
        }, directorId);
    }
}