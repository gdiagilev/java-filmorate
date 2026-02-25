package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Film add(Film film);

    Film update(Film film);

    Film getById(int id);

    List<Film> getAll();

    void delete(int filmId);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    List<Film> getPopularFilms(int count, Integer genreId, Integer year);

    List<Film> getCommonFilms(int userId, int friendId);

    List<Film> getRecommendations(int userId);

    List<Film> search(String query, List<String> by);

    List<Film> getFilmsByDirectorIdSortedByYear(int directorId);

    List<Film> getFilmsByDirectorIdSortedByLikes(int directorId);

    List<Film> getTopLikedFilms(int count);

    Optional<Film> getByIdOptional(int id);

    boolean existsById(int id);
}