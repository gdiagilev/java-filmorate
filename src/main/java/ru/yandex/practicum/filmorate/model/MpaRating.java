package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class MpaRating {

    private final int id;
    private final String name;

    public MpaRating(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static MpaRating fromId(int id) {
        switch (id) {
            case 1: return new MpaRating(1, "G");
            case 2: return new MpaRating(2, "PG");
            case 3: return new MpaRating(3, "PG-13");
            case 4: return new MpaRating(4, "R");
            case 5: return new MpaRating(5, "NC-17");
            default: throw new IllegalArgumentException("MPA с id=" + id + " не найден");
        }
    }
}