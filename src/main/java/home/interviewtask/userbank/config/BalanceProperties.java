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
package home.interviewtask.userbank.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.balance")
public class BalanceProperties {

    /** Доля, добавляемая за один цикл (0.10 = +10%). */
    private BigDecimal accrualRate = new BigDecimal("0.10");

    /** Потолок как множитель первоначального депозита (2.07 = 207%). */
    private BigDecimal maxMultiplier = new BigDecimal("2.07");

    private long schedulerFixedRateMs = 30_000L;
}
