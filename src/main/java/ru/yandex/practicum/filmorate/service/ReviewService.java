package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final EventService eventService;

    public Review add(Review review, UserService userService, FilmService filmService) {
        validateReview(review);

        if (!userService.existsById(review.getUserId())) {
            throw new NotFoundException("Пользователь с id=" + review.getUserId() + " не найден");
        }

        if (!filmService.existsById(review.getFilmId())) {
            throw new NotFoundException("Фильм с id=" + review.getFilmId() + " не найден");
        }

        return reviewStorage.add(review);
    }

    private void validateReview(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new ValidationException("Содержимое отзыва не может быть пустым");
        }
        if (review.getIsPositive() == null) {
            throw new ValidationException("Нужно указать положительный или отрицательный отзыв");
        }
    }

    public Review update(Review review) {
        Review existing = reviewStorage.getReview(review.getReviewId());
        if (existing == null) {
            throw new NotFoundException("Review with id=" + review.getReviewId() + " not found");
        }

        validateReview(review);

        Review updated = reviewStorage.update(review);

        eventService.addEvent(
                review.getUserId(),
                EventType.REVIEW,
                Operation.UPDATE,
                updated.getReviewId()
        );

        return updated;
    }

    public void delete(int id) {
        Review review = reviewStorage.getReview(id);
        if (review == null) {
            throw new NotFoundException("Review with id=" + id + " not found");
        }
        reviewStorage.delete(id);

        eventService.addEvent(
                review.getUserId(),
                EventType.REVIEW,
                Operation.REMOVE,
                review.getReviewId()
        );
    }

    public Review getById(int id) {
        Review review = reviewStorage.getReview(id);
        if (review == null) {
            throw new NotFoundException("Review with id=" + id + " not found");
        }
        return review;
    }

    public List<Review> getReviews(Integer filmId, int count) {
        if (filmId != null && filmId > 0 && !filmStorage.existsById(filmId)) {
            throw new ValidationException("Invalid filmId");
        }
        return reviewStorage.getReviews(filmId != null ? filmId : 0, count);
    }

    public void addLike(int reviewId, int userId) {
        validateUser(userId);
        reviewStorage.addLike(reviewId, userId);
    }

    public void removeLike(int reviewId, int userId) {
        validateUser(userId);
        reviewStorage.removeLike(reviewId, userId);
    }

    public void addDislike(int reviewId, int userId) {
        validateUser(userId);
        reviewStorage.addDislike(reviewId, userId);
    }

    public void removeDislike(int reviewId, int userId) {
        validateUser(userId);
        reviewStorage.removeDislike(reviewId, userId);
    }

    private void validateUser(int userId) {
        if (userId <= 0 || !userStorage.existsById(userId)) {
            throw new ValidationException("Invalid userId");
        }
    }
}