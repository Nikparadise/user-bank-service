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
package home.interviewtask.userbank.api.exception;

/** Конфликт уникальности, например email/телефон уже используется другим пользователем. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
