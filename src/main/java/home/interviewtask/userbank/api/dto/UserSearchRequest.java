/*
 * Copyright (C) 2026 Milkov Nikita. All rights reserved.
 *
 * Organisation: home
 * Developer:    Milkov Nikita
 * Email:        nikparadise@mail.ru
 * Mobile:       8-920-130-6265
 *
 * Created:      29.05.2026
 * Last edited:  30.05.2026 15:05
 * Edited by:    Milkov Nikita
 */
package home.interviewtask.userbank.api.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Необязательные фильтры поиска. Можно передать любую комбинацию; отсутствующие поля игнорируются.
 */
@Data
public class UserSearchRequest {

    /** Записи, у которых date_of_birth строго больше переданного значения. */
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate dateOfBirth;

    /** Точное совпадение (100%). */
    private String phone;

    /** LIKE '{name}%'. */
    private String name;

    /** Точное совпадение (100%). */
    private String email;
}
