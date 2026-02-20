package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorStorage {

    Director create(Director director);

    Director update(Director director);

    Director getById(int id);

    List<Director> getAll();

    boolean deleteById(int id);
}
