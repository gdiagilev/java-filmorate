package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final JdbcTemplate jdbcTemplate;

    private final EventStorage eventStorage;

    public List<Event> getFeed(long userId) {
        return eventStorage.getUserFeed(userId);
    }

    public void addEvent(int userId, EventType eventType, Operation operation, int entityId) {
        String sql = "INSERT INTO events (user_id, event_type, operation, entity_id, timestamp) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                userId,
                eventType.name(),
                operation.name(),
                entityId,
                System.currentTimeMillis());
    }
}