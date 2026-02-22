package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Director create(@RequestBody Director director) {
        log.info("Получен POST /directors: {}", director);
        return directorService.create(director);
    }

    @PutMapping
    public Director update(@RequestBody Director director) {
        log.info("Получен PUT /directors: {}", director);
        return directorService.update(director);
    }

    @GetMapping("/{id}")
    public Director getById(@PathVariable int id) {
        log.info("Получен GET /directors/{}", id);
        Director result = directorService.getById(id);
        log.info("Отдан ответ GET /directors/{}: {}", id, result);
        return result;
    }

    @GetMapping
    public List<Director> getAll() {
        log.info("Получен GET /directors");
        List<Director> result = directorService.getAll();
        log.info("Отдан ответ GET /directors: {}", result);
        return result;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public boolean deleteById(@PathVariable int id) {
        return directorService.deleteById(id);
    }
}
