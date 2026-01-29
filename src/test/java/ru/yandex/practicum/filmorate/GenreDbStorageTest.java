package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreDbStorage;

    @BeforeEach
    void setup() {
    }

    @Test
    void testGetAll() {
        List<Genre> genres = genreDbStorage.getAll();
        assertFalse(genres.isEmpty());
        assertTrue(genres.stream().anyMatch(g -> g.getName().equals("Комедия")));
    }

    @Test
    void testGetById() {
        Genre genre = genreDbStorage.getById(1);
        assertEquals(1, genre.getId());
        assertEquals("Комедия", genre.getName());
    }

    @Test
    void testGetByIdInvalid() {
        Exception exception = assertThrows(Exception.class, () -> {
            genreDbStorage.getById(999);
        });
    }
}