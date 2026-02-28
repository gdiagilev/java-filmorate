package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreDbStorage genreDbStorage;

    @GetMapping
    public List<Genre> getAll() {
        log.info("Получен GET /genres");
        List<Genre> result = genreDbStorage.getAll();
        log.info("Отдан ответ GET /genres: {}", result);
        return result;
    }

    @GetMapping("/{id}")
    public Genre getById(@PathVariable int id) {
        log.info("Получен GET /genres/{}", id);
        Genre result = genreDbStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id=" + id + " не найден"));
        log.info("Отдан ответ GET /genres/{}: {}", id, result);
        return result;
    }
}