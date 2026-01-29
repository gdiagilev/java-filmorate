package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Arrays;
import java.util.List;

@Component
public class MpaDbStorage {

    public List<MpaRating> getAll() {
        return Arrays.asList(MpaRating.values());
    }

    public MpaRating getById(int id) {
        return MpaRating.fromId(id);
    }
}