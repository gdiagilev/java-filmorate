package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final EventService eventService;

    // ================= ФИЛЬМЫ =================

    public Film create(Film film) {
        validateFilm(film);
        enrichFilm(film);
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        filmStorage.getById(film.getId()); // проверяем существование
        validateFilm(film);
        enrichFilm(film);
        return filmStorage.update(film);
    }

    public Film getById(int id) {
        return filmStorage.getById(id);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public void deleteFilm(int filmId) {
        filmStorage.getById(filmId); // проверяем существование
        filmStorage.delete(filmId);
    }

    // ================= ЛАЙКИ =================

    public void addLike(int filmId, int userId) {
        filmStorage.addLike(filmId, userId);
        eventService.addEvent(userId, EventType.LIKE, Operation.ADD, filmId);
    }

    public void removeLike(int filmId, int userId) {
        filmStorage.removeLike(filmId, userId);
        eventService.addEvent(userId, EventType.LIKE, Operation.REMOVE, filmId);
    }

    // ================= ПОПУЛЯРНЫЕ / ОБЩИЕ ФИЛЬМЫ =================

    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        return filmStorage.getPopularFilms(count, genreId, year);
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

    // ================= ВАЛИДАЦИЯ / ОБОГАЩЕНИЕ =================

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new IllegalArgumentException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new IllegalArgumentException("Описание фильма не может быть длиннее 200 символов");
        }

        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new IllegalArgumentException("Дата релиза фильма не может быть раньше " + CINEMA_BIRTHDAY);
        }

        if (film.getDuration() == null || film.getDuration() <= 0) {
            throw new IllegalArgumentException("Продолжительность фильма должна быть положительной");
        }

        if (film.getMpa() == null) {
            throw new IllegalArgumentException("MPA рейтинг фильма должен быть указан");
        }
    }

    private void enrichFilm(Film film) {
        // MPA
        MpaRating mpa = mpaStorage.getById(film.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("MPA с id=" + film.getMpa().getId() + " не найден"));
        film.setMpa(mpa);

        // Жанры
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            film.setGenres(film.getGenres().stream()
                    .map(genre -> genreStorage.getById(genre.getId())
                            .orElseThrow(() -> new NotFoundException("Genre с id=" + genre.getId() + " не найден")))
                    .sorted(Comparator.comparingInt(Genre::getId))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }
    }
}