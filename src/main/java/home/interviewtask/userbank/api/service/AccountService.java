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

import home.interviewtask.userbank.api.dto.AccountResponse;
import home.interviewtask.userbank.postgresql.entity.Account;
import home.interviewtask.userbank.api.exception.NotFoundException;
import home.interviewtask.userbank.postgresql.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    /** Всегда отдаётся актуальным (без кэша), так как баланс меняется каждые 30 секунд. */
    @Transactional(readOnly = true)
    public AccountResponse getBalance(Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Account not found for user: " + userId));
        return new AccountResponse(userId, account.getBalance(), account.getInitialBalance());
    }
}
