package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    public Review add(Review review) {
        validateReview(review);

        if (!userStorage.existsById(review.getUserId())) {
            throw new NotFoundException("Пользователь с id=" + review.getUserId() + " не найден");
        }
        if (!filmStorage.existsById(review.getFilmId())) {
            throw new NotFoundException("Фильм с id=" + review.getFilmId() + " не найден");
        }

        Review created = reviewStorage.add(review);
        eventService.addEvent(review.getUserId(), EventType.REVIEW, Operation.ADD, created.getReviewId());
        return created;
    }

    public Review update(Review review) {
        Review existing = reviewStorage.getReview(review.getReviewId());
        if (existing == null) throw new NotFoundException("Отзыв с id=" + review.getReviewId() + " не найден");

        validateReview(review);
        Review updated = reviewStorage.update(review);
        eventService.addEvent(review.getUserId(), EventType.REVIEW, Operation.UPDATE, updated.getReviewId());
        return updated;
    }

    public void delete(int id) {
        Review review = reviewStorage.getReview(id);
        if (review == null) throw new NotFoundException("Отзыв с id=" + id + " не найден");

        reviewStorage.delete(id);
        eventService.addEvent(review.getUserId(), EventType.REVIEW, Operation.REMOVE, review.getReviewId());
    }

    public Review getById(int id) {
        return reviewStorage.getReview(id);
    }

    public List<Review> getReviews(Integer filmId, int count) {
        if (filmId != null && filmId > 0 && !filmStorage.existsById(filmId)) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        return reviewStorage.getReviews(filmId != null ? filmId : 0, count);
    }

    public void addLike(int reviewId, Integer userId) {
        validateUser(userId);
        reviewStorage.addLike(reviewId, userId);
        eventService.addEvent(userId, EventType.REVIEW, Operation.ADD, reviewId);
    }

    public void removeLike(int reviewId, Integer userId) {
        validateUser(userId);
        reviewStorage.removeLike(reviewId, userId);
        eventService.addEvent(userId, EventType.REVIEW, Operation.REMOVE, reviewId);
    }

    public void addDislike(int reviewId, Integer userId) {
        validateUser(userId);
        reviewStorage.addDislike(reviewId, userId);
        eventService.addEvent(userId, EventType.REVIEW, Operation.REMOVE, reviewId);
    }

    public void removeDislike(int reviewId, Integer userId) {
        validateUser(userId);
        reviewStorage.removeDislike(reviewId, userId);
        eventService.addEvent(userId, EventType.REVIEW, Operation.ADD, reviewId);
    }

    private void validateUser(Integer userId) {
        if (userId == null || userId <= 0 || !userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    private void validateReview(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new IllegalArgumentException("Review content cannot be empty");
        }
        if (review.getIsPositive() == null) {
            throw new IllegalArgumentException("Review like/dislike must be specified");
        }
    }
}