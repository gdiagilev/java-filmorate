package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum MpaRating {
    G, PG, PG_13, R, NC_17;

    @JsonCreator
    public static MpaRating from(String value) {
        return MpaRating.valueOf(value.toUpperCase());
    }
}