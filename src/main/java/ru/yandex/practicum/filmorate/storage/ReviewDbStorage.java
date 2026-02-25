package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review add(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, 0)";
        KeyHolder keyHolder = new GeneratedKeyHolder(); // Spring KeyHolder

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setInt(3, review.getUserId());
            ps.setInt(4, review.getFilmId());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) throw new IllegalStateException("Не удалось получить ID для отзыва");
        review.setReviewId(key.intValue());

        return getReview(review.getReviewId());
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content=?, is_positive=? WHERE id=?";
        int updated = jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());
        if (updated == 0) throw new NotFoundException("Отзыв с id=" + review.getReviewId() + " не найден");
        return getReview(review.getReviewId());
    }

    @Override
    public void delete(int reviewId) {
        int deleted = jdbcTemplate.update("DELETE FROM reviews WHERE id=?", reviewId);
        if (deleted == 0) throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
    }

    @Override
    public Review getReview(int reviewId) {
        List<Review> reviews = jdbcTemplate.query(
                "SELECT id, content, is_positive, user_id, film_id, useful FROM reviews WHERE id=?",
                (rs, rowNum) -> {
                    Review r = new Review();
                    r.setReviewId(rs.getInt("id"));
                    r.setContent(rs.getString("content"));
                    r.setIsPositive(rs.getBoolean("is_positive"));
                    r.setUserId(rs.getInt("user_id"));
                    r.setFilmId(rs.getInt("film_id"));
                    r.setUseful(rs.getInt("useful"));
                    return r;
                },
                reviewId
        );
        if (reviews.isEmpty()) throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        return reviews.get(0);
    }

    @Override
    public List<Review> getReviews(int filmId, int count) {
        String sql;
        Object[] params;
        if (filmId > 0) {
            sql = "SELECT id, content, is_positive, user_id, film_id, useful FROM reviews " +
                    "WHERE film_id=? ORDER BY useful DESC LIMIT ?";
            params = new Object[]{filmId, count};
        } else {
            sql = "SELECT id, content, is_positive, user_id, film_id, useful FROM reviews " +
                    "ORDER BY useful DESC LIMIT ?";
            params = new Object[]{count};
        }
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Review r = new Review();
            r.setReviewId(rs.getInt("id"));
            r.setContent(rs.getString("content"));
            r.setIsPositive(rs.getBoolean("is_positive"));
            r.setUserId(rs.getInt("user_id"));
            r.setFilmId(rs.getInt("film_id"));
            r.setUseful(rs.getInt("useful"));
            return r;
        }, params);
    }

    @Override
    public void addLike(int reviewId, int userId) {
        String checkSql = "SELECT COUNT(*) FROM review_likes WHERE review_id=? AND user_id=?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, reviewId, userId);
        if (count == null || count == 0) {
            jdbcTemplate.update("INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, TRUE)", reviewId, userId);
            jdbcTemplate.update("UPDATE reviews SET useful = useful + 1 WHERE id=?", reviewId);
        }
    }

    @Override
    public void removeLike(int reviewId, int userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM review_likes WHERE review_id=? AND user_id=? AND is_like=TRUE",
                Integer.class, reviewId, userId);
        if (count != null && count > 0) {
            jdbcTemplate.update("DELETE FROM review_likes WHERE review_id=? AND user_id=?", reviewId, userId);
            jdbcTemplate.update("UPDATE reviews SET useful = useful - 1 WHERE id=?", reviewId);
        }
    }

    @Override
    public void addDislike(int reviewId, int userId) {
        String checkSql = "SELECT COUNT(*) FROM review_likes WHERE review_id=? AND user_id=?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, reviewId, userId);
        if (count == null || count == 0) {
            jdbcTemplate.update("INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, FALSE)", reviewId, userId);
            jdbcTemplate.update("UPDATE reviews SET useful = useful - 1 WHERE id=?", reviewId);
        }
    }

    @Override
    public void removeDislike(int reviewId, int userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM review_likes WHERE review_id=? AND user_id=? AND is_like=FALSE",
                Integer.class, reviewId, userId);
        if (count != null && count > 0) {
            jdbcTemplate.update("DELETE FROM review_likes WHERE review_id=? AND user_id=?", reviewId, userId);
            jdbcTemplate.update("UPDATE reviews SET useful = useful + 1 WHERE id=?", reviewId);
        }
    }
}