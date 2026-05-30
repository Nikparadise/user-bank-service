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

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotNull(message = "toUserId обязателен")
    private Long toUserId;

    @NotNull(message = "value обязателен")
    @DecimalMin(value = "0.01", message = "value должен быть положительным")
    @Digits(integer = 17, fraction = 2, message = "value должен иметь не более 2 знаков после запятой")
    private BigDecimal value;
}
