package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventStorage eventStorage;

    public List<Event> getFeed(long userId) {
        return eventStorage.getUserFeed(userId);
    }

    public void addEvent(int userId, EventType eventType, Operation operation, long entityId) {
        Event event = new Event();
        event.setUserId(userId);
        event.setEventType(eventType);
        event.setOperation(operation);
        event.setEntityId(entityId);
        event.setTimestamp(System.currentTimeMillis());
        eventStorage.addEvent(event);
    }
}