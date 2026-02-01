package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {

    private final GenreDbStorage genreDbStorage;

    @Test
    void shouldGetAllGenres() {
        List<Genre> genres = genreDbStorage.getAll();

        assertEquals(6, genres.size());

        Genre comedy = null;
        for (Genre genre : genres) {
            if (genre.getId() == 1) {
                comedy = genre;
                break;
            }
        }

        assertNotNull(comedy);
        assertEquals("Комедия", comedy.getName());
    }

    @Test
    void shouldGetGenreById() {
        Genre genre = genreDbStorage.getById(1);

        assertNotNull(genre);
        assertEquals(1, genre.getId());
        assertEquals("Комедия", genre.getName());
    }
}