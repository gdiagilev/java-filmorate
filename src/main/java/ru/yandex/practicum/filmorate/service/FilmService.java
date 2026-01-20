package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film create(Film film) {
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        getFilmOrThrow(film.getId());
        return filmStorage.update(film);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(int id) {
        return getFilmOrThrow(id);
    }

    public void addLike(int filmId, int userId) {
        Film film = getFilmOrThrow(filmId);
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        film.getLikes().add(userId);
    }

    public void removeLike(int filmId, int userId) {
        Film film = getFilmOrThrow(filmId);
        if (userStorage.getById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        film.getLikes().remove(userId);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    private Film getFilmOrThrow(int id) {
        Film film = filmStorage.getById(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        return film;
    }

    public Set<Genre> getFilmGenres(int filmId) {
        return getFilmOrThrow(filmId).getGenres();
    }

    public MpaRating getFilmMpa(int filmId) {
        return getFilmOrThrow(filmId).getMpa();
    }
}