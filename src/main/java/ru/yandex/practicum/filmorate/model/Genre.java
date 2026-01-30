package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Genre {
    private int id;
    private String name;

    public static Genre fromId(int id) {
        switch (id) {
            case 1: return new Genre(1, "Комедия");
            case 2: return new Genre(2, "Драма");
            case 3: return new Genre(3, "Боевик");
            case 4: return new Genre(4, "Триллер");
            case 5: return new Genre(5, "Ужасы");
            case 6: return new Genre(6, "Фэнтези");
            case 7: return new Genre(7, "Мелодрама");
            case 8: return new Genre(8, "Документальный");
            default: throw new IllegalArgumentException("Жанр с id=" + id + " не найден");
        }
    }
}