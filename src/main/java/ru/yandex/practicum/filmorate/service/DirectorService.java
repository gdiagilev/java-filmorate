package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorStorage directorStorage;

    public Director create(Director director) {
        if (director.getName() == null || director.getName().isBlank()) {
            throw new IllegalArgumentException("Имя и фамилия режиссёра не могут быть пустыми");
        }

        return directorStorage.create(director);
    }

    public Director update(Director director) {
        directorStorage.getById(director.getId());
        if (director.getName() == null || director.getName().isBlank()) {
            throw new IllegalArgumentException("Имя и фамилия режиссёра не могут быть пустыми");
        }

        return directorStorage.update(director);
    }

    public Director getById(int id) {
        return directorStorage.getById(id);
    }

    public List<Director> getAll() {
        return directorStorage.getAll();
    }

    public boolean deleteById(int id) {
        directorStorage.getById(id);
        return directorStorage.deleteById(id);
    }
}
