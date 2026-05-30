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

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class EmailRequest {

    @NotBlank(message = "email не должен быть пустым")
    @Email(message = "email должен быть корректным адресом")
    @Size(max = 200, message = "email должен быть не длиннее 200 символов")
    private String email;
}
