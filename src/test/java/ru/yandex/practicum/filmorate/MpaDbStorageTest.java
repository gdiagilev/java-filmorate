package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {

    private final MpaDbStorage mpaDbStorage;

    @Test
    void shouldGetAllMpaRatings() {
        List<MpaRating> ratings = mpaDbStorage.getAll();

        assertEquals(5, ratings.size());
        assertEquals("G", ratings.get(0).getName());
    }

    @Test
    void shouldGetMpaById() {
        Optional<MpaRating> rating = mpaDbStorage.getById(3);

        assertTrue(rating.isPresent());
        assertEquals(3, rating.get().getId());
        assertEquals("PG-13", rating.get().getName());
    }

    @Test
    void shouldReturnEmptyIfMpaNotFound() {
        Optional<MpaRating> rating = mpaDbStorage.getById(999);

        assertTrue(rating.isEmpty());
    }
}