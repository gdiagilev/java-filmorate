package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Event addEvent(Event event) {
        String sql = """
                INSERT INTO events (timestamp, user_id, event_type, operation, entity_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"event_id"});
            ps.setLong(1, event.getTimestamp());
            ps.setLong(2, event.getUserId());
            ps.setString(3, event.getEventType().name());
            ps.setString(4, event.getOperation().name());
            ps.setLong(5, event.getEntityId());
            return ps;
        }, keyHolder);

        event.setEventId(keyHolder.getKey().longValue());
        return event;
    }

    @Override
    public List<Event> getUserFeed(long userId) {
        String sql = """
                SELECT event_id, user_id, event_type, operation, entity_id, timestamp
                FROM events
                WHERE user_id = ?
                ORDER BY timestamp ASC
                """;

        return jdbcTemplate.query(sql, this::mapRow, userId);
    }

    private Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        Event event = new Event();
        event.setEventId(rs.getLong("event_id"));
        event.setUserId(rs.getLong("user_id"));
        event.setEventType(Enum.valueOf(ru.yandex.practicum.filmorate.model.EventType.class,
                rs.getString("event_type")));
        event.setOperation(Enum.valueOf(ru.yandex.practicum.filmorate.model.Operation.class,
                rs.getString("operation")));
        event.setEntityId(rs.getLong("entity_id"));
        event.setTimestamp(rs.getLong("timestamp"));
        return event;
    }
}