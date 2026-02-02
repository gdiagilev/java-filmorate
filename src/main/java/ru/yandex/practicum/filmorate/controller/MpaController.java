package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaStorage mpaStorage;

    @GetMapping
    public List<MpaRating> getAll() {
        log.info("Получен GET /mpa");
        List<MpaRating> result = mpaStorage.getAll();
        log.info("Отдан ответ GET /mpa: {}", result);
        return result;
    }

    @GetMapping("/{id}")
    public MpaRating getById(@PathVariable int id) {
        log.info("Получен GET /mpa/{}", id);
        MpaRating result = mpaStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("MPA с id=" + id + " не найден"));
        log.info("Отдан ответ GET /mpa/{}: {}", id, result);
        return result;
    }
}