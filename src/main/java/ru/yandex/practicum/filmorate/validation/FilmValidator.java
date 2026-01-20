package ru.yandex.practicum.filmorate.validation;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class FilmValidator {

    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public static void validate(Film film) {
        if (film == null) {
            throw new ValidationException("Фильм не может быть null");
        }

        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации фильма: пустое название");
            throw new ValidationException("Название фильма не может быть пустым.");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            log.warn("Ошибка валидации фильма: длина описания {} символов", film.getDescription().length());
            throw new ValidationException("Описание фильма не должно превышать 200 символов.");
        }

        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Ошибка валидации фильма: слишком ранняя дата релиза {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28.12.1895.");
        }

        if (film.getDuration() <= 0) {
            log.warn("Ошибка валидации фильма: некорректная продолжительность {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }

        if (film.getMpa() == null) {
            log.warn("Ошибка валидации фильма: MPA рейтинг не задан");
            throw new ValidationException("MPA рейтинг не задан");
        }

        if (film.getGenres() == null) {
            film.setGenres(new HashSet<>());
        }

        Set<Genre> genres = film.getGenres();
        if (genres != null && genres.contains(null)) {
            log.warn("Ошибка валидации фильма: список жанров содержит null");
            throw new ValidationException("Список жанров содержит null");
        }
    }
}