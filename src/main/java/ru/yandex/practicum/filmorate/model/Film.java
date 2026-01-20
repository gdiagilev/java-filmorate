package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Data
public class Film {
    private int id;

    @NotBlank
    private String name;

    @Size(max = 200)
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    @Positive
    private int duration;

    private Set<Integer> likes = new HashSet<>();

    private MpaRating mpa;

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    private Set<Genre> genres = new HashSet<>();
}