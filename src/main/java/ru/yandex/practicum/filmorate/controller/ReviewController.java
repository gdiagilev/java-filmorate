package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private final FilmService filmService;
    private final EventService eventService;

    @PostMapping
    public Review createReview(@RequestBody Review review) {
        validateIds(review);
        return reviewService.add(review);
    }

    @PutMapping
    public Review updateReview(@RequestBody Review review) {
        validateIds(review);
        return reviewService.update(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable int id) {
        reviewService.delete(id);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable int id) {
        return reviewService.getById(id);
    }

    @GetMapping
    public List<Review> getReviews(@RequestParam(required = false) Integer filmId,
                                   @RequestParam(defaultValue = "10") int count) {
        return reviewService.getReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable int id, @PathVariable int userId) {
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable int id, @PathVariable int userId) {
        reviewService.removeDislike(id, userId);
    }

    private void validateIds(Review review) {
        if (review.getUserId() <= 0) {
            throw new ValidationException("userId должен быть положительным");
        }
        if (review.getFilmId() <= 0) {
            throw new ValidationException("filmId должен быть положительным");
        }
        if (!userService.existsById(review.getUserId())) {
            throw new NotFoundException("Пользователь с id=" + review.getUserId() + " не найден");
        }
        if (!filmService.existsById(review.getFilmId())) {
            throw new NotFoundException("Фильм с id=" + review.getFilmId() + " не найден");
        }
    }
}