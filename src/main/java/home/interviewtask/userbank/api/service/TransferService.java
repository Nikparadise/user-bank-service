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
package home.interviewtask.userbank.api.service;

import home.interviewtask.userbank.postgresql.entity.Account;
import home.interviewtask.userbank.api.exception.BusinessException;
import home.interviewtask.userbank.api.exception.NotFoundException;
import home.interviewtask.userbank.postgresql.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Перевод денег между двумя пользователями. Считается высокозначимой банковской операцией:
 * полностью валидируется, транзакционна и потокобезопасна.
 *
 * <p>Потокобезопасность: обе строки счетов берутся под пессимистичной блокировкой на запись
 * (SELECT ... FOR UPDATE). Блокировки всегда захватываются в едином глобальном порядке
 * (сначала меньший id пользователя), поэтому конкурентные переводы между одной и той же парой
 * пользователей не приводят к взаимоблокировке. Дополнительно БД гарантирует CHECK-ограничение
 * {@code balance >= 0} как последний рубеж защиты.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;

    @Transactional
    public void transfer(Long fromUserId, Long toUserId, BigDecimal value) {
        if (fromUserId.equals(toUserId)) {
            throw new BusinessException("Cannot transfer money to yourself");
        }
        if (value == null || value.signum() <= 0) {
            throw new BusinessException("Transfer value must be positive");
        }

        // Захватываем блокировки в едином порядке, чтобы избежать взаимоблокировок.
        long firstId = Math.min(fromUserId, toUserId);
        long secondId = Math.max(fromUserId, toUserId);
        Account first = lockAccount(firstId);
        Account second = lockAccount(secondId);

        Account from = fromUserId == firstId ? first : second;
        Account to = fromUserId == firstId ? second : first;

        if (from.getBalance().compareTo(value) < 0) {
            throw new BusinessException("Insufficient funds");
        }

        from.setBalance(from.getBalance().subtract(value));
        to.setBalance(to.getBalance().add(value));

        log.info("Transfer of {} from user id={} to user id={} completed", value, fromUserId, toUserId);
    }

    private Account lockAccount(long userId) {
        return accountRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new NotFoundException("Account not found for user: " + userId));
    }
}
