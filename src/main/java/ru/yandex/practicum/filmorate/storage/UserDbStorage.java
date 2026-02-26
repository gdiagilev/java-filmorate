package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Component
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("users").usingGeneratedKeyColumns("id");
    }

    @Override
    public User add(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        HashMap<String, Object> values = new HashMap<>();
        values.put("email", user.getEmail());
        values.put("login", user.getLogin());
        values.put("name", user.getName());
        values.put("birthday", Date.valueOf(user.getBirthday()));

        Number id = jdbcInsert.executeAndReturnKey(values);
        user.setId(id.intValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), Date.valueOf(user.getBirthday()), user.getId());
        if (updated == 0) {
            throw new RuntimeException("User not found with id " + user.getId());
        }
        return user;
    }

    @Override
    public Optional<User> getById(int id) {
        String sql = "SELECT id, email, login, name, birthday FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setEmail(rs.getString("email"));
            u.setLogin(rs.getString("login"));
            u.setName(rs.getString("name"));
            u.setBirthday(rs.getDate("birthday").toLocalDate());
            return u;
        }, id);
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<User> getByIdOptional(int id) {
        String sql = "SELECT id, email, login, name, birthday FROM users WHERE id = ?";
        List<User> result = jdbcTemplate.query(sql, (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setEmail(rs.getString("email"));
            u.setLogin(rs.getString("login"));
            u.setName(rs.getString("name"));
            u.setBirthday(rs.getDate("birthday").toLocalDate());
            return u;
        }, id);

        if (result.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(result.get(0));
    }

    @Override
    public List<User> getAll() {
        String sql = "SELECT id, email, login, name, birthday FROM users";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setEmail(rs.getString("email"));
            u.setLogin(rs.getString("login"));
            u.setName(rs.getString("name"));
            u.setBirthday(rs.getDate("birthday").toLocalDate());
            return u;
        });
    }


    @Override
    public boolean addFriend(int userId, int friendId) {
        String checkSql = "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, friendId);

        if (count != null && count > 0) {
            return false;
        }

        String insertSql = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(insertSql, userId, friendId);

        return true;
    }

    @Override
    public boolean removeFriend(int userId, int friendId) {
        String deleteSql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        return jdbcTemplate.update(deleteSql, userId, friendId) > 0;
    }

    @Override
    public List<User> getFriends(int userId) {
        String sql = "SELECT u.id, u.email, u.login, u.name, u.birthday " + "FROM users u " + "JOIN friendships f ON u.id = f.friend_id " + "WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setEmail(rs.getString("email"));
            u.setLogin(rs.getString("login"));
            u.setName(rs.getString("name"));
            u.setBirthday(rs.getDate("birthday").toLocalDate());
            return u;
        }, userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        String sql = "SELECT u.id, u.email, u.login, u.name, u.birthday " + "FROM users u " + "JOIN friendships f1 ON u.id = f1.friend_id " + "JOIN friendships f2 ON u.id = f2.friend_id " + "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User u = new User();
            u.setId(rs.getInt("id"));
            u.setEmail(rs.getString("email"));
            u.setLogin(rs.getString("login"));
            u.setName(rs.getString("name"));
            u.setBirthday(rs.getDate("birthday").toLocalDate());
            return u;
        }, userId, otherId);
    }

    @Override
    public void delete(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, userId);
    }

    @Override
    public boolean existsById(int id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}