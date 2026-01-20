package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ReleaseDateNotBeforeValidator implements ConstraintValidator<ReleaseDateNotBefore, LocalDate> {

    private static final LocalDate MIN_DATE = LocalDate.of(1895, 12, 28);

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) return true; // null допустим, если не требуется @NotNull
        return !value.isBefore(MIN_DATE);
    }
}