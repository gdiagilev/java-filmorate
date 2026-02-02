package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {

    private final MpaDbStorage mpaDbStorage;

    @Test
    void shouldGetAllMpaRatings() {
        List<MpaRating> ratings = mpaDbStorage.getAll();

        assertEquals(5, ratings.size());

        MpaRating gRating = ratings.stream()
                .filter(r -> r.getId() == 1)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("MPA G не найден"));

        assertEquals("G", gRating.getName());
    }

    @Test
    void shouldGetMpaById() {
        MpaRating rating = mpaDbStorage.getById(3)
                .orElseThrow(() -> new NotFoundException("MPA с id=3 не найден"));

        assertEquals(3, rating.getId());
        assertEquals("PG-13", rating.getName());
    }

    @Test
    void shouldReturnEmptyIfMpaNotFound() {
        assertTrue(mpaDbStorage.getById(999).isEmpty());
    }
}