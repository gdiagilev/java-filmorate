package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserService userService;
    private final FilmService filmService;
    private final EventService eventService;

    public Review add(Review review) {
        validateReview(review);
        Review created = reviewStorage.add(review);
        eventService.addEvent(review.getUserId(), EventType.REVIEW, Operation.ADD, created.getReviewId());
        return created;
    }

    public Review update(Review review) {
        reviewStorage.getReview(review.getReviewId());
        Review updated = reviewStorage.update(review);
        eventService.addEvent(review.getUserId(), EventType.REVIEW, Operation.UPDATE, updated.getReviewId());
        return updated;
    }

    public void delete(int id) {
        Review review = reviewStorage.getReview(id);
        reviewStorage.delete(id);
        eventService.addEvent(review.getUserId(), EventType.REVIEW, Operation.REMOVE, review.getReviewId());
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
        userService.getById(review.getUserId());
        filmService.getById(review.getFilmId());
    }

    private void validateUser(int userId) {
        try {
            userService.getById(userId);
        } catch (NotFoundException e) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден");
        }
    }
}