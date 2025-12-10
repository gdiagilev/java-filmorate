package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.FilmValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final List<Film> films = new ArrayList<>();
    private int nextFilmId = 1;

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);

        if (film == null) {
            log.warn("Ошибка: пустое тело запроса при добавлении фильма");
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        FilmValidator.validate(film);

        film.setId(nextFilmId++);
        films.add(film);

        log.info("Фильм успешно добавлен: id={}", film.getId());
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("Получен запрос на обновление фильма: {}", film);

        if (film == null) {
            log.warn("Ошибка: пустое тело запроса при обновлении фильма");
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        FilmValidator.validate(film);

        Optional<Film> existing = films.stream()
                .filter(f -> f.getId() == film.getId())
                .findFirst();

        if (existing.isEmpty()) {
            log.warn("Фильм с id={} не найден", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        films.remove(existing.get());
        films.add(film);

        log.info("Фильм успешно обновлён: id={}", film.getId());
        return film;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Получен запрос на получение списка всех фильмов ({} шт.)", films.size());
        return films;
    }
}
