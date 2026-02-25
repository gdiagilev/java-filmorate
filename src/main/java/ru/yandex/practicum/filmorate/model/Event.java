package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private long eventId;
    private long userId;
    private EventType eventType;
    private Operation operation;
    private long entityId;
    private long timestamp;
}