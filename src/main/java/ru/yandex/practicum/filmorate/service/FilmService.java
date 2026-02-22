package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final DirectorService directorService;

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;

    public Film create(Film film) {
        validateFilm(film);
        enrichFilm(film);
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        filmStorage.getById(film.getId());

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
        MpaRating mpa = mpaStorage.getById(film.getMpa().getId())
                .orElseThrow(() ->
                        new NotFoundException("MPA с id=" + film.getMpa().getId() + " не найден"));
        film.setMpa(mpa);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            film.setGenres(
                    film.getGenres().stream()
                            .map(genre -> genreStorage.getById(genre.getId())
                                    .orElseThrow(() ->
                                            new NotFoundException("Genre с id=" + genre.getId() + " не найден")))
                            .sorted(Comparator.comparingInt(Genre::getId))
                            .collect(Collectors.toCollection(LinkedHashSet::new))
            );
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            film.getDirectors().forEach(director -> {
                Director found = directorStorage.getById(director.getId());
                director.setName(found.getName());
            });
        }
    }

    public List<Film> getFilmsByDirector(int directorId, String sortBy) {
        if (directorId <= 0) {
            throw new IllegalArgumentException("ID режиссёра должно быть положительным числом");
        }

        if (sortBy.equals("year")) {
            return filmStorage.getFilmsByDirectorIdSortedByYear(directorId);
        } else if (sortBy.equals("likes")) {
            return filmStorage.getFilmsByDirectorIdSortedByLikes(directorId);
        } else {
            throw new IllegalArgumentException("sortBy должен быть 'year' или 'likes'");
        }
    }
    public List<Film> getRecommendations(int userId) {
        return filmStorage.getRecommendations(userId);
    }
}