package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final EventService eventService;

    public Review add(Review review) {
        validateReview(review);
        Review created = reviewStorage.add(review);
        eventService.addEvent(review.getUserId(), EventType.REVIEW, Operation.ADD, created.getReviewId());
        return created;
    }

    public Review update(Review review) {
        validateReview(review);
        Review updated = reviewStorage.update(review);
        eventService.addEvent(review.getUserId(), EventType.REVIEW, Operation.UPDATE, updated.getReviewId());
        return updated;
    }

    public void delete(int id) {
        Review review = reviewStorage.getReview(id);
        if (review == null) throw new NotFoundException("Отзыв с id=" + id + " не найден");
        reviewStorage.delete(id);
        eventService.addEvent(review.getUserId(), EventType.REVIEW, Operation.REMOVE, id);
    }

    public Review getById(int id) {
        Review review = reviewStorage.getReview(id);
        if (review == null) throw new NotFoundException("Отзыв с id=" + id + " не найден");
        return review;
    }

    public List<Review> getReviews(Integer filmId, int count) {
        return reviewStorage.getReviews(filmId != null ? filmId : 0, count);
    }

    public void addLike(int reviewId, int userId) {
        Review review = reviewStorage.getReview(reviewId);
        if (review == null) throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        reviewStorage.addLike(reviewId, userId);
        eventService.addEvent(userId, EventType.REVIEW, Operation.ADD, reviewId);
    }

    public void removeLike(int reviewId, int userId) {
        reviewStorage.removeLike(reviewId, userId);
        eventService.addEvent(userId, EventType.REVIEW, Operation.REMOVE, reviewId);
    }

    public void addDislike(int reviewId, int userId) {
        Review review = reviewStorage.getReview(reviewId);
        if (review == null) throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        reviewStorage.addDislike(reviewId, userId);
        eventService.addEvent(userId, EventType.REVIEW, Operation.REMOVE, reviewId);
    }

    public void removeDislike(int reviewId, int userId) {
        reviewStorage.removeDislike(reviewId, userId);
        eventService.addEvent(userId, EventType.REVIEW, Operation.ADD, reviewId);
    }

    private void validateReview(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new ValidationException("Содержимое отзыва не может быть пустым");
        }
        if (review.getIsPositive() == null) {
            throw new ValidationException("Нужно указать положительный или отрицательный отзыв");
        }
    }
}