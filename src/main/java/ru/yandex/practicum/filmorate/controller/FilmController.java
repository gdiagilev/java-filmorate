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
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        log.info("Получен GET /films/popular?count={}", count);
        List<Film> result = filmService.getPopularFilms(count);
        log.info("Отдан ответ GET /films/popular: {}", result);
        return result;
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(
            @PathVariable int directorId,
            @RequestParam(name = "sortBy", defaultValue = "") String sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }
    @GetMapping("/search")
    public List<Film> search(@RequestParam String query,
                             @RequestParam(required = false) String by) {
        log.info("Получен GET /films/search?query={}&by={}", query, by);
        List<Film> result = filmService.search(query, by);
        log.info("Отдан ответ GET /films/search: {} фильмов", result.size());
        return result;
    }
}