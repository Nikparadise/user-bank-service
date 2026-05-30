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
package home.interviewtask.userbank.api.controller;

import home.interviewtask.userbank.api.dto.AccountResponse;
import home.interviewtask.userbank.api.dto.TransferRequest;
import home.interviewtask.userbank.api.service.AccountService;
import home.interviewtask.userbank.api.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Счёт и переводы")
public class AccountController {

    private final AccountService accountService;
    private final TransferService transferService;

    @GetMapping("/me")
    @Operation(summary = "Текущий баланс авторизованного пользователя (всегда актуальный)")
    public AccountResponse myBalance(@AuthenticationPrincipal Long currentUserId) {
        return accountService.getBalance(currentUserId);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Перевод денег от авторизованного пользователя (USER_ID из токена) другому пользователю")
    public ResponseEntity<Void> transfer(@AuthenticationPrincipal Long currentUserId,
                                         @Valid @RequestBody TransferRequest request) {
        transferService.transfer(currentUserId, request.getToUserId(), request.getValue());
        return ResponseEntity.ok().build();
    }
}
