package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
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
        if (key == null) throw new IllegalStateException("Ошибка генерации ID фильма");
        film.setId(key.intValue());

        saveGenres(film);
        saveDirectors(film);

        return getById(film.getId());
    }

    @Override
    public Film update(Film film) {
        getById(film.getId());
        removeDuplicateGenres(film);

        String sql = "UPDATE films SET name=?, description=?, release_date=?, duration=?, mpa_id=? WHERE id=?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        updateFilmGenres(film);
        updateFilmDirectors(film);

        return getById(film.getId());
    }

    @Override
    public Film getById(int id) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                "FROM films f JOIN mpa m ON f.mpa_id = m.id WHERE f.id=?";

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapFilmWithRelations(rs), id);

        if (films.isEmpty()) throw new NotFoundException("Фильм с id=" + id + " не найден");
        return films.get(0);
    }

    public Optional<Film> getByIdOptional(int id) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                "FROM films f JOIN mpa m ON f.mpa_id = m.id WHERE f.id=?";

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapFilmWithRelations(rs), id);

        return films.isEmpty() ? Optional.empty() : Optional.of(films.get(0));
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                "FROM films f JOIN mpa m ON f.mpa_id = m.id ORDER BY f.id ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapFilmWithRelations(rs));
    }

    @Override
    public void delete(int filmId) {
        jdbcTemplate.update("DELETE FROM films WHERE id=?", filmId);
    }

    @Override
    public void addLike(int filmId, int userId) {
        getById(filmId);
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM film_likes WHERE film_id=? AND user_id=?", Integer.class, filmId, userId);
        if (count == null || count == 0) {
            jdbcTemplate.update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
        }
    }

    @Override
    public void removeLike(int filmId, int userId) {
        getById(filmId);
        jdbcTemplate.update("DELETE FROM film_likes WHERE film_id=? AND user_id=?", filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        StringBuilder sql = new StringBuilder(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name, " +
                        "COUNT(fl.user_id) AS likes " +
                        "FROM films f " +
                        "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                        "JOIN mpa m ON f.mpa_id = m.id");

        if (genreId != null) sql.append(" JOIN film_genres fg ON f.id = fg.film_id");
        sql.append(" WHERE 1=1");
        if (genreId != null) sql.append(" AND fg.genre_id=").append(genreId);
        if (year != null) sql.append(" AND EXTRACT(YEAR FROM f.release_date)=").append(year);
        sql.append(" GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name ");
        sql.append(" ORDER BY likes DESC LIMIT ?");

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> mapFilmWithRelations(rs), count);
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                "FROM films f JOIN film_likes l1 ON f.id=l1.film_id JOIN film_likes l2 ON f.id=l2.film_id " +
                "JOIN mpa m ON f.mpa_id=m.id WHERE l1.user_id=? AND l2.user_id=? " +
                "ORDER BY (SELECT COUNT(*) FROM film_likes fl WHERE fl.film_id=f.id) DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapFilmWithRelations(rs), userId, friendId);
    }

    @Override
    public List<Film> getRecommendations(int userId) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                "FROM films f JOIN mpa m ON f.mpa_id=m.id " +
                "WHERE f.id NOT IN (SELECT film_id FROM film_likes WHERE user_id=?) LIMIT 10";

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapFilmWithRelations(rs), userId);
    }

    @Override
    public List<Film> search(String query, List<String> fields) {
        StringBuilder where = new StringBuilder();
        List<Object> params = new ArrayList<>();
        if (fields.contains("title") && fields.contains("director")) {
            where.append("(LOWER(f.name) LIKE LOWER(?) OR LOWER(d.name) LIKE LOWER(?))");
            params.add("%" + query + "%");
            params.add("%" + query + "%");
        } else if (fields.contains("title")) {
            where.append("LOWER(f.name) LIKE LOWER(?)");
            params.add("%" + query + "%");
        } else if (fields.contains("director")) {
            where.append("LOWER(d.name) LIKE LOWER(?)");
            params.add("%" + query + "%");
        } else {
            where.append("LOWER(f.name) LIKE LOWER(?)");
            params.add("%" + query + "%");
        }

        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                "FROM films f JOIN mpa m ON f.mpa_id=m.id " +
                "LEFT JOIN film_directors fd ON f.id=fd.film_id " +
                "LEFT JOIN directors d ON fd.director_id=d.id " +
                "WHERE " + where + " GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name";

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapFilmWithRelations(rs), params.toArray());
    }

    @Override
    public List<Film> getFilmsByDirectorIdSortedByYear(int directorId) {
        List<Integer> filmIds = jdbcTemplate.queryForList(
                "SELECT f.id FROM films f " +
                        "JOIN film_directors fd ON f.id = fd.film_id " +
                        "WHERE fd.director_id = ? " +
                        "ORDER BY f.release_date ASC",
                Integer.class,
                directorId
        );
        return getFilmsByIds(filmIds);
    }

    @Override
    public List<Film> getFilmsByDirectorIdSortedByLikes(int directorId) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name, " +
                "COUNT(l.user_id) AS like_count " +
                "FROM films f " +
                "JOIN film_directors fd ON f.id=fd.film_id " +
                "LEFT JOIN film_likes l ON f.id=l.film_id " +
                "JOIN mpa m ON f.mpa_id=m.id " +
                "WHERE fd.director_id=? " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
                "ORDER BY like_count DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapFilmWithRelations(rs), directorId);
    }

    private List<Film> getFilmsByIds(List<Integer> filmIds) {
        List<Film> films = new ArrayList<>();
        for (Integer id : filmIds) {
            try {
                films.add(getById(id));
            } catch (NotFoundException e) {
                // ignore
            }
        }
        return films;
    }

    private Film mapFilmWithRelations(ResultSet rs) throws SQLException {
        Film f = new Film();
        f.setId(rs.getInt("id"));
        f.setName(rs.getString("name"));
        f.setDescription(rs.getString("description"));

        Date date = rs.getDate("release_date");
        f.setReleaseDate(date != null ? date.toLocalDate() : LocalDate.of(1900, 1, 1));

        f.setDuration(rs.getInt("duration"));

        MpaRating mpa = new MpaRating();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));
        f.setMpa(mpa);

        f.setGenres(Optional.ofNullable(getGenresByFilmId(f.getId())).orElse(new LinkedHashSet<>()));
        f.setDirectors(Optional.ofNullable(getDirectorsByFilmId(f.getId())).orElse(new LinkedHashSet<>()));

        return f;
    }

    private void removeDuplicateGenres(Film film) {
        if (film.getGenres() != null) {
            Set<Integer> ids = new HashSet<>();
            Set<Genre> unique = new LinkedHashSet<>();
            for (Genre g : film.getGenres()) {
                if (ids.add(g.getId())) unique.add(g);
            }
            film.setGenres(unique);
        }
    }

    private void saveGenres(Film film) {
        if (film.getGenres() != null) {
            for (Genre g : film.getGenres()) {
                jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", film.getId(), g.getId());
            }
        }
    }

    private void updateFilmGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id=?", film.getId());
        saveGenres(film);
    }

    private Set<Genre> getGenresByFilmId(int filmId) {
        String sql = "SELECT g.id, g.name FROM genres g JOIN film_genres fg ON g.id=fg.genre_id WHERE fg.film_id=?";
        return new LinkedHashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre g = new Genre();
            g.setId(rs.getInt("id"));
            g.setName(rs.getString("name"));
            return g;
        }, filmId));
    }

    private void saveDirectors(Film film) {
        if (film.getDirectors() != null) {
            for (Director d : film.getDirectors()) {
                jdbcTemplate.update("INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)", film.getId(), d.getId());
            }
        }
    }

    private void updateFilmDirectors(Film film) {
        jdbcTemplate.update("DELETE FROM film_directors WHERE film_id=?", film.getId());
        saveDirectors(film);
    }

    private Set<Director> getDirectorsByFilmId(int filmId) {
        String sql = "SELECT d.id, d.name FROM directors d JOIN film_directors fd ON d.id=fd.director_id WHERE fd.film_id=?";
        return new LinkedHashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> {
            Director d = new Director();
            d.setId(rs.getInt("id"));
            d.setName(rs.getString("name"));
            return d;
        }, filmId));
    }

    @Override
    public List<Film> getTopLikedFilms(int count) {
        return getPopularFilms(count, null, null);
    }

    @Override
    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}