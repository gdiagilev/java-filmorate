package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Получен POST /films: {}", film);
        Film result = filmService.create(film);
        log.info("Отдан ответ POST /films: {}", result);
        return result;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Получен PUT /films: {}", film);
        Film result = filmService.update(film);
        log.info("Отдан ответ PUT /films: {}", result);
        return result;
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable int id) {
        log.info("Получен GET /films/{} ", id);
        Film result = filmService.getById(id);
        log.info("Отдан ответ GET /films/{}: {}", id, result);
        return result;
    }

    @GetMapping
    public List<Film> getAll() {
        log.info("Получен GET /films");
        List<Film> result = filmService.getAll();
        log.info("Отдан ответ GET /films: {}", result);
        return result;
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Получен PUT /films/{}/like/{}", id, userId);
        filmService.addLike(id, userId);
        log.info("Отдан ответ PUT /films/{}/like/{}: OK", id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Получен DELETE /films/{}/like/{}", id, userId);
        filmService.removeLike(id, userId);
        log.info("Отдан ответ DELETE /films/{}/like/{}: OK", id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year) {

        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam int userId,
                                     @RequestParam int friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable int filmId) {
        filmService.deleteFilm(filmId);
    }
}