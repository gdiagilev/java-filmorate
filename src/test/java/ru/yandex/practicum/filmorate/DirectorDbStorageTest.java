package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DirectorDbStorageTest {

    private final DirectorDbStorage directorDbStorage;

    @Test
    void shouldAddDirector() {
        Director director = new Director();
        director.setName("Christopher Nolan");

        Director saved = directorDbStorage.add(director);

        assertNotNull(saved.getId());
        assertEquals("Christopher Nolan", saved.getName());
    }

    @Test
    void shouldUpdateDirector() {
        Director director = new Director();
        director.setName("Quentin Tarantino");
        Director saved = directorDbStorage.add(director);

        saved.setName("Quentin Tarantino Updated");
        directorDbStorage.update(saved);

        Optional<Director> updated = directorDbStorage.getById(saved.getId());
        assertTrue(updated.isPresent());
        assertEquals("Quentin Tarantino Updated", updated.get().getName());
    }

    @Test
    void shouldGetDirectorById() {
        Director director = new Director();
        director.setName("Martin Scorsese");
        Director saved = directorDbStorage.add(director);

        Optional<Director> found = directorDbStorage.getById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("Martin Scorsese", found.get().getName());
    }

    @Test
    void shouldReturnEmptyIfDirectorNotFound() {
        Optional<Director> found = directorDbStorage.getById(999);
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldGetAllDirectors() {
        Director director1 = new Director();
        director1.setName("Steven Spielberg");
        Director director2 = new Director();
        director2.setName("James Cameron");

        directorDbStorage.add(director1);
        directorDbStorage.add(director2);

        List<Director> all = directorDbStorage.getAll();
        assertEquals(2, all.size());
    }

    @Test
    void shouldDeleteDirector() {
        Director director = new Director();
        director.setName("Ridley Scott");
        Director saved = directorDbStorage.add(director);

        directorDbStorage.delete(saved.getId());

        Optional<Director> deleted = directorDbStorage.getById(saved.getId());
        assertTrue(deleted.isEmpty());
    }
}