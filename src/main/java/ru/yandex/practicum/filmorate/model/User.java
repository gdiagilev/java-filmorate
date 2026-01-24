package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
public class User {
    private int id;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String login;
    private String name;

    @PastOrPresent
    private LocalDate birthday;

    private Map<Integer, FriendshipStatus> friends = new HashMap<>();
}