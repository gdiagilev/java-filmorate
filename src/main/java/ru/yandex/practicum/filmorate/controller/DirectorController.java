package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    public List<Director> getAll() {
        log.info("Получен GET /directors");
        List<Director> result = directorService.getAll();
        log.info("Отдан ответ GET /directors: {}", result);
        return result;
    }

    @GetMapping("/{id}")
    public Director getById(@PathVariable int id) {
        log.info("Получен GET /directors/{}", id);
        Director result = directorService.getById(id);
        log.info("Отдан ответ GET /directors/{}: {}", id, result);
        return result;
    }

    @PostMapping
    public Director create(@Valid @RequestBody Director director) {
        log.info("Получен POST /directors: {}", director);
        Director result = directorService.add(director);
        log.info("Отдан ответ POST /directors: {}", result);
        return result;
    }

    @PutMapping
    public Director update(@Valid @RequestBody Director director) {
        log.info("Получен PUT /directors: {}", director);
        Director result = directorService.update(director);
        log.info("Отдан ответ PUT /directors: {}", result);
        return result;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        log.info("Получен DELETE /directors/{}", id);
        directorService.delete(id);
        log.info("Отдан ответ DELETE /directors/{}: OK", id);
    }
}