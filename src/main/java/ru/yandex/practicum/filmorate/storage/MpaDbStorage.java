package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class MpaDbStorage implements MpaStorage {

    private final List<MpaRating> ratings = Arrays.asList(
            MpaRating.fromId(1),
            MpaRating.fromId(2),
            MpaRating.fromId(3),
            MpaRating.fromId(4),
            MpaRating.fromId(5)
    );

    @Override
    public List<MpaRating> getAll() {
        return ratings;
    }

    @Override
    public Optional<MpaRating> getById(int id) {
        return ratings.stream()
                .filter(r -> r.getId() == id)
                .findFirst();
    }
}