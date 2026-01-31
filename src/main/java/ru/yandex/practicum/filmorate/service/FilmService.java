package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private final FilmStorage filmStorage;

    public Film create(Film film) {
        validateFilm(film);
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        filmStorage.getById(film.getId()); // проверка существования
        validateFilm(film);
        return filmStorage.update(film);
    }

    public Film getById(int id) {
        return filmStorage.getById(id);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public void addLike(int filmId, int userId) {
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getTopLikedFilms(count);
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

        try {
            MpaRating.fromId(film.getMpa().getId());
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("MPA с id=" + film.getMpa().getId() + " не найден");
        }
    }
}