package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {

    Film add(Film film);

    Film update(Film film);

    Film getById(int id);

    List<Film> getAll();

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    List<Film> getTopLikedFilms(int count);

    List<Film> getFilmsByDirectorIdSortedByYear(int id);

    List<Film> getFilmsByDirectorIdSortedByLikes(int id);

    List<Film> getRecommendations(int userId);

    List<Film> search(String query, List<String> fields);
}