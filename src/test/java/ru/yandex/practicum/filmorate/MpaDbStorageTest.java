package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class MpaDbStorageTest {

    @Autowired
    private MpaDbStorage mpaDbStorage;

    @Test
    void testGetAll() {
        List<MpaRating> allRatings = mpaDbStorage.getAll();
        assertEquals(5, allRatings.size());
        assertTrue(allRatings.contains(MpaRating.G));
        assertTrue(allRatings.contains(MpaRating.PG_13));
    }

    @Test
    void testGetById() {
        MpaRating rating = mpaDbStorage.getById(3);
        assertEquals(MpaRating.PG_13, rating);
    }

    @Test
    void testGetByIdInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            mpaDbStorage.getById(999);
        });
        assertTrue(exception.getMessage().contains("MPA с id=999 не найден"));
    }
}