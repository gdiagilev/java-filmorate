package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private static final RowMapper<Review> REVIEW_ROW_MAPPER = (rs, rowNum) -> {
        Review review = new Review();
        review.setReviewId(rs.getInt("id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setUserId(rs.getInt("user_id"));
        review.setFilmId(rs.getInt("film_id"));
        review.setUseful(rs.getInt("useful"));
        return review;
    };
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review add(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, 0)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getFilmId());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Не удалось получить сгенерированный ID для отзыва");
        }
        review.setReviewId(key.intValue());
        log.info("Добавлен отзыв id={} от пользователя id={} на фильм id={}", review.getReviewId(), review.getUserId(), review.getFilmId());
        return review;
    }

    @Override
    public Review update(Review review) {
        getReview(review.getReviewId());
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?";
        jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());
        log.info("Обновлён отзыв id={}", review.getReviewId());
        return getReview(review.getReviewId());
    }

    @Override
    public void delete(int id) {
        getReview(id);
        jdbcTemplate.update("DELETE FROM reviews WHERE id = ?", id);
        log.info("Удалён отзыв id={}", id);
    }

    @Override
    public Review getReview(int id) {
        String sql = "SELECT id, content, is_positive, user_id, film_id, useful FROM reviews WHERE id = ?";
        List<Review> reviews = jdbcTemplate.query(sql, REVIEW_ROW_MAPPER, id);
        if (reviews.isEmpty()) {
            throw new NotFoundException("Отзыв с id=" + id + " не найден");
        }
        return reviews.getFirst();
    }

    @Override
    public List<Review> getReviews(int filmId, int count) {
        if (filmId > 0) {
            String sql = "SELECT id, content, is_positive, user_id, film_id, useful " +
                    "FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
            log.debug("Запрос отзывов для фильма id={}, limit={}", filmId, count);
            return jdbcTemplate.query(sql, REVIEW_ROW_MAPPER, filmId, count);
        }
        String sql = "SELECT id, content, is_positive, user_id, film_id, useful " +
                "FROM reviews ORDER BY useful DESC LIMIT ?";
        log.debug("Запрос всех отзывов, limit={}", count);
        return jdbcTemplate.query(sql, REVIEW_ROW_MAPPER, count);
    }

    @Override
    @Transactional
    public void addLike(int reviewId, int userId) {
        getReview(reviewId);
        ReactionType existing = getExistingReaction(reviewId, userId);

        if (existing == ReactionType.LIKE) {
            throw new IllegalStateException(
                    "Пользователь id=" + userId + " уже поставил лайк отзыву id=" + reviewId
            );
        }
        if (existing == ReactionType.DISLIKE) {
            jdbcTemplate.update(
                    "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?", reviewId, userId
            );
            jdbcTemplate.update("UPDATE reviews SET useful = useful + 1 WHERE id = ?", reviewId);
            log.info("Автоматически удалён дизлайк пользователя id={} с отзыва id={}", userId, reviewId);
        }

        jdbcTemplate.update(
                "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, TRUE)", reviewId, userId
        );
        jdbcTemplate.update("UPDATE reviews SET useful = useful + 1 WHERE id = ?", reviewId);
        log.info("Пользователь id={} поставил лайк отзыву id={}", userId, reviewId);
    }

    @Override
    public void removeLike(int reviewId, int userId) {
        int affected = jdbcTemplate.update(
                "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = TRUE",
                reviewId, userId
        );
        if (affected == 0) {
            throw new NotFoundException("Лайк пользователя id=" + userId + " на отзыв id=" + reviewId + " не найден");
        }
        jdbcTemplate.update("UPDATE reviews SET useful = useful - 1 WHERE id = ?", reviewId);
        log.info("Пользователь id={} убрал лайк с отзыва id={}", userId, reviewId);
    }

    @Override
    @Transactional
    public void addDislike(int reviewId, int userId) {
        getReview(reviewId);
        ReactionType existing = getExistingReaction(reviewId, userId);

        if (existing == ReactionType.DISLIKE) {
            throw new IllegalStateException(
                    "Пользователь id=" + userId + " уже поставил дизлайк отзыву id=" + reviewId
            );
        }
        if (existing == ReactionType.LIKE) {
            jdbcTemplate.update(
                    "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?", reviewId, userId
            );
            jdbcTemplate.update("UPDATE reviews SET useful = useful - 1 WHERE id = ?", reviewId);
            log.info("Автоматически удалён лайк пользователя id={} с отзыва id={}", userId, reviewId);
        }

        jdbcTemplate.update(
                "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, FALSE)", reviewId, userId
        );
        jdbcTemplate.update("UPDATE reviews SET useful = useful - 1 WHERE id = ?", reviewId);
        log.info("Пользователь id={} поставил дизлайк отзыву id={}", userId, reviewId);
    }

    @Override
    public void removeDislike(int reviewId, int userId) {
        int affected = jdbcTemplate.update(
                "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = FALSE",
                reviewId, userId
        );
        if (affected == 0) {
            throw new NotFoundException("Дизлайк пользователя id=" + userId + " на отзыв id=" + reviewId + " не найден");
        }
        jdbcTemplate.update("UPDATE reviews SET useful = useful + 1 WHERE id = ?", reviewId);
        log.info("Пользователь id={} убрал дизлайк с отзыва id={}", userId, reviewId);
    }

    private ReactionType getExistingReaction(int reviewId, int userId) {
        String sql = "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?";
        List<Boolean> result = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getBoolean("is_like"), reviewId, userId);
        if (result.isEmpty()) return null;
        return result.getFirst() ? ReactionType.LIKE : ReactionType.DISLIKE;
    }

    private enum ReactionType {LIKE, DISLIKE}
}
