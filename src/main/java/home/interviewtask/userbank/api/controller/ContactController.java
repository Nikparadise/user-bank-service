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

import home.interviewtask.userbank.api.dto.EmailRequest;
import home.interviewtask.userbank.api.dto.IdResponse;
import home.interviewtask.userbank.api.dto.PhoneRequest;
import home.interviewtask.userbank.api.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * Самостоятельное управление пользователем своими контактами.
 * Владелец всегда берётся из JWT (USER_ID), а не из тела запроса.
 */
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@Tag(name = "Мои контакты (email / телефон)")
public class ContactController {

    private final ContactService contactService;

    // ---------- email ----------

    @GetMapping("/emails")
    @Operation(summary = "Список моих email")
    public List<String> listEmails(@AuthenticationPrincipal Long userId) {
        return contactService.listEmails(userId);
    }

    @PostMapping("/emails")
    @Operation(summary = "Добавить email (не должен быть занят другим пользователем)")
    public ResponseEntity<IdResponse> addEmail(@AuthenticationPrincipal Long userId,
                                               @Valid @RequestBody EmailRequest request) {
        Long id = contactService.addEmail(userId, request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(new IdResponse(id));
    }

    @PutMapping("/emails/{emailId}")
    @Operation(summary = "Изменить один из моих email")
    public ResponseEntity<Void> changeEmail(@AuthenticationPrincipal Long userId,
                                            @PathVariable Long emailId,
                                            @Valid @RequestBody EmailRequest request) {
        contactService.changeEmail(userId, emailId, request.getEmail());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/emails/{emailId}")
    @Operation(summary = "Удалить один из моих email (минимум один должен остаться)")
    public ResponseEntity<Void> deleteEmail(@AuthenticationPrincipal Long userId,
                                            @PathVariable Long emailId) {
        contactService.deleteEmail(userId, emailId);
        return ResponseEntity.noContent().build();
    }

    // ---------- телефоны ----------

    @GetMapping("/phones")
    @Operation(summary = "Список номеров моих телефонов")
    public List<String> listPhones(@AuthenticationPrincipal Long userId) {
        return contactService.listPhones(userId);
    }

    @PostMapping("/phones")
    @Operation(summary = "Добавить номер телефона (не должен быть занят другим пользователем)")
    public ResponseEntity<IdResponse> addPhone(@AuthenticationPrincipal Long userId,
                                               @Valid @RequestBody PhoneRequest request) {
        Long id = contactService.addPhone(userId, request.getPhone());
        return ResponseEntity.status(HttpStatus.CREATED).body(new IdResponse(id));
    }

    @PutMapping("/phones/{phoneId}")
    @Operation(summary = "Изменить один из моих номеров телефонов")
    public ResponseEntity<Void> changePhone(@AuthenticationPrincipal Long userId,
                                            @PathVariable Long phoneId,
                                            @Valid @RequestBody PhoneRequest request) {
        contactService.changePhone(userId, phoneId, request.getPhone());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/phones/{phoneId}")
    @Operation(summary = "Удалить один из моих номеров телефонов (минимум один должен остаться)")
    public ResponseEntity<Void> deletePhone(@AuthenticationPrincipal Long userId,
                                            @PathVariable Long phoneId) {
        contactService.deletePhone(userId, phoneId);
        return ResponseEntity.noContent().build();
    }
}
