package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

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

        jdbcTemplate.update(sql,
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType(),
                event.getOperation(),
                event.getEntityId());

        return event;
    }

    @Override
    public List<Event> getUserFeed(long userId) {
        String sql = """
                    SELECT * FROM events
                    WHERE user_id = ?
                    ORDER BY timestamp
                """;

        return jdbcTemplate.query(sql, this::mapRow, userId);
    }

    private Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        Event event = new Event();
        event.setEventId(rs.getLong("event_id"));
        event.setTimestamp(rs.getLong("timestamp"));
        event.setUserId(rs.getLong("user_id"));
        event.setEventType(rs.getString("event_type"));
        event.setOperation(rs.getString("operation"));
        event.setEntityId(rs.getLong("entity_id"));
        return event;
    }
}