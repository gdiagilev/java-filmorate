package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final JdbcTemplate jdbcTemplate;

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;
    private final DirectorService directorService;
    private final EventService eventService;

    public Film create(Film film) {
        setDefaultMpaIfNull(film);
        validateFilm(film);

        checkMpa(film);
        checkGenres(film);

        enrichFilm(film);
        Film savedFilm = filmStorage.add(film);

        Film maybeFilm = filmStorage.getById(savedFilm.getId());
        if (maybeFilm == null) {
            throw new NotFoundException("Film not found after creation");
        }
        return maybeFilm;
    }

    public Film update(Film film) {
        Film existingFilm = filmStorage.getById(film.getId());
        if (existingFilm == null) {
            throw new NotFoundException("Film not found");
        }

        setDefaultMpaIfNull(film);
        validateFilm(film);

        checkMpa(film);
        checkGenres(film);

        enrichFilm(film);
        filmStorage.update(film);

        Film updatedFilm = filmStorage.getById(film.getId());
        if (updatedFilm == null) {
            throw new NotFoundException("Film not found after update");
        }
        return updatedFilm;
    }

    public Film getById(int id) {
        return filmStorage.getById(id);
    }

    public Optional<Film> getByIdOptional(int id) {
        return filmStorage.getByIdOptional(id);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public void deleteFilm(int filmId) {
        filmStorage.getById(filmId); // проверка существования
        filmStorage.delete(filmId);
    }

    public void addLike(int filmId, int userId) {
        filmStorage.addLike(filmId, userId);
        eventService.addEvent(userId, EventType.LIKE, Operation.ADD, filmId);
    }

    public void removeLike(int filmId, int userId) {
        filmStorage.removeLike(filmId, userId);
        eventService.addEvent(userId, EventType.LIKE, Operation.REMOVE, filmId);
    }

    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        return filmStorage.getPopularFilms(count, genreId, year);
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> getRecommendations(int userId) {
        return filmStorage.getRecommendations(userId);
    }

    public List<Film> search(String query, String by) {
        return filmStorage.search(query, Arrays.asList(by.split(",")));
    }

    public void addDirectorToFilm(int filmId, int directorId) {
        Film film = filmStorage.getById(filmId);
        if (film == null) throw new NotFoundException("Фильм с id=" + filmId + " не найден");

        Director director = directorStorage.getById(directorId);
        if (director == null) throw new NotFoundException("Режиссёр с id=" + directorId + " не найден");

        if (film.getDirectors() == null) film.setDirectors(new LinkedHashSet<>());
        film.getDirectors().add(director);

        filmStorage.update(film);
    }

    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        Director director = directorStorage.getById(directorId);
        if (director == null) throw new NotFoundException("Режиссёр с id=" + directorId + " не найден");

        List<Film> films;
        if ("year".equals(sortBy)) {
            films = filmStorage.getFilmsByDirectorIdSortedByYear(directorId);
        } else if ("likes".equals(sortBy) || "rate".equals(sortBy)) {
            films = filmStorage.getFilmsByDirectorIdSortedByLikes(directorId);
        } else {
            throw new IllegalArgumentException("sortBy должен быть 'year' или 'likes'");
        }

        if (films == null) films = new ArrayList<>();
        return films;
    }

    private void checkMpa(Film film) {
        if (film.getMpa() != null && mpaStorage.getById(film.getMpa().getId()).isEmpty()) {
            throw new NotFoundException("MPA not found");
        }
    }

    private void checkGenres(Film film) {
        if (film.getGenres() != null) {
            for (var genre : film.getGenres()) {
                if (genreStorage.getById(genre.getId()).isEmpty()) {
                    throw new NotFoundException("Genre not found");
                }
            }
        }
    }

    private void setDefaultMpaIfNull(Film film) {
        if (film.getMpa() == null) {
            var defaultMpa = mpaStorage.getById(1)
                    .orElseThrow(() -> new NotFoundException("Default MPA not found"));
            film.setMpa(defaultMpa);
        }
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank())
            throw new IllegalArgumentException("Название фильма не может быть пустым");

        if (film.getDescription() != null && film.getDescription().length() > 200)
            throw new IllegalArgumentException("Описание фильма не может быть длиннее 200 символов");

        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(CINEMA_BIRTHDAY))
            throw new IllegalArgumentException("Дата релиза фильма не может быть раньше " + CINEMA_BIRTHDAY);

        if (film.getDuration() == null || film.getDuration() <= 0)
            throw new IllegalArgumentException("Продолжительность фильма должна быть положительной");

        if (film.getMpa() == null)
            throw new IllegalArgumentException("MPA рейтинг фильма должен быть указан");
    }

    private void enrichFilm(Film film) {
        MpaRating mpa = mpaStorage.getById(film.getMpa().getId())
                .orElseThrow(() -> new NoSuchElementException("MPA с id=" + film.getMpa().getId() + " не найден"));
        film.setMpa(mpa);

        if (film.getGenres() != null) film.setGenres(new LinkedHashSet<>(film.getGenres()));
        else film.setGenres(new LinkedHashSet<>());

        Set<Director> enriched = new LinkedHashSet<>();
        if (film.getDirectors() != null) {
            for (Director d : film.getDirectors()) {
                Director found = directorStorage.getById(d.getId());
                if (found != null) enriched.add(found);
            }
        }
        film.setDirectors(enriched);
    }

    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id=?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public boolean filmExists(int filmId) {
        return filmStorage.getByIdOptional(filmId).isPresent();
    }
}