package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public List<Director> getAll() {
        return directorStorage.getAll();
    }

    public Director getById(int id) {
        return directorStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id=" + id + " не найден"));
    }

    public Director add(Director director) {
        if (director.getName() == null || director.getName().isBlank()) {
            throw new IllegalArgumentException("Имя режиссёра не может быть пустым");
        }
        return directorStorage.add(director);
    }

    public Director update(Director director) {
        getById(director.getId()); // проверка существования
        if (director.getName() == null || director.getName().isBlank()) {
            throw new IllegalArgumentException("Имя режиссёра не может быть пустым");
        }
        return directorStorage.update(director);
    }

    public void delete(int id) {
        getById(id); // проверка существования
        directorStorage.delete(id);
    }
}