package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {

    Review add(Review review);

    Review update(Review review);

    void delete(int id);

    Review getReview(int id);

    List<Review> getReviews(int filmId, int count);

    void addLike(int reviewId, int userId);

    void removeLike(int reviewId, int userId);

    void addDislike(int reviewId, int userId);

    void removeDislike(int reviewId, int userId);
}
