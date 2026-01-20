package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Genre {
    COMEDY, DRAMA, CARTOON, THRILLER, DOCUMENTARY, ACTION;

    @JsonCreator
    public static Genre from(String value) {
        return Genre.valueOf(value.toUpperCase());
    }
}