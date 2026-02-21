package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserService userService;
    private final FilmService filmService;

    public Review add(Review review) {
        validateReview(review);
        return reviewStorage.add(review);
    }

    public Review update(Review review) {
        reviewStorage.getReview(review.getReviewId());
        return reviewStorage.update(review);
    }

    public void delete(int id) {
        reviewStorage.delete(id);
    }

    public Review getById(int id) {
        return reviewStorage.getReview(id);
    }

    public List<Review> getReviews(Integer filmId, int count) {
        if (filmId != null) {
            filmService.getById(filmId);
        }
        return reviewStorage.getReviews(filmId != null ? filmId : 0, count);
    }

    public void addLike(int id, int userId) {
        validateUser(userId);
        reviewStorage.addLike(id, userId);
    }

    public void removeLike(int id, int userId) {
        validateUser(userId);
        reviewStorage.removeLike(id, userId);
    }

    public void addDislike(int id, int userId) {
        validateUser(userId);
        reviewStorage.addDislike(id, userId);
    }

    public void removeDislike(int id, int userId) {
        validateUser(userId);
        reviewStorage.removeDislike(id, userId);
    }

    private void validateReview(Review review) {
        Integer userId = review.getUserId();
        Integer filmId = review.getFilmId();

        if (userId == null || userId == 0) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден");
        }
        if (filmId == null || filmId == 0) {
            throw new ValidationException("Фильм с id=" + filmId + " не найден");
        }
        userService.getById(userId);
        filmService.getById(filmId);
    }

    private void validateUser(int userId) {
        try {
            userService.getById(userId);
        } catch (NotFoundException e) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден");
        }
    }
}

