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

import home.interviewtask.userbank.postgresql.entity.EmailData;
import home.interviewtask.userbank.postgresql.entity.PhoneData;
import home.interviewtask.userbank.postgresql.entity.User;
import home.interviewtask.userbank.api.exception.BusinessException;
import home.interviewtask.userbank.api.exception.ConflictException;
import home.interviewtask.userbank.api.exception.NotFoundException;
import home.interviewtask.userbank.postgresql.repository.EmailDataRepository;
import home.interviewtask.userbank.postgresql.repository.PhoneDataRepository;
import home.interviewtask.userbank.postgresql.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Пользователь может добавлять/менять/удалять только СВОИ email/телефон и только если значение
 * не занято другим пользователем. Всегда должны оставаться хотя бы один email и один телефон.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

    private final UserRepository userRepository;
    private final EmailDataRepository emailRepository;
    private final PhoneDataRepository phoneRepository;

    // ---------- чтение ----------

    @Transactional(readOnly = true)
    public List<String> listEmails(Long userId) {
        return emailRepository.findAllByUserId(userId).stream()
                .map(EmailData::getEmail).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> listPhones(Long userId) {
        return phoneRepository.findAllByUserId(userId).stream()
                .map(PhoneData::getPhone).collect(Collectors.toList());
    }

    // ---------- email ----------

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#userId"),
            @CacheEvict(value = "userSearch", allEntries = true)})
    public Long addEmail(Long userId, String email) {
        ensureEmailFree(email);
        User user = requireUser(userId);
        EmailData entity = EmailData.builder().user(user).email(email).build();
        Long id = emailRepository.save(entity).getId();
        log.info("User id={} added email id={}", userId, id);
        return id;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#userId"),
            @CacheEvict(value = "userSearch", allEntries = true)})
    public void changeEmail(Long userId, Long emailId, String newEmail) {
        EmailData entity = emailRepository.findById(emailId)
                .orElseThrow(() -> new NotFoundException("Email not found: " + emailId));
        requireOwnership(entity.getUser().getId(), userId, "email");
        if (!entity.getEmail().equals(newEmail)) {
            ensureEmailFree(newEmail);
            entity.setEmail(newEmail);
        }
        log.info("User id={} changed email id={}", userId, emailId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#userId"),
            @CacheEvict(value = "userSearch", allEntries = true)})
    public void deleteEmail(Long userId, Long emailId) {
        EmailData entity = emailRepository.findById(emailId)
                .orElseThrow(() -> new NotFoundException("Email not found: " + emailId));
        requireOwnership(entity.getUser().getId(), userId, "email");
        if (emailRepository.countByUserId(userId) <= 1) {
            throw new BusinessException("A user must keep at least one email");
        }
        emailRepository.delete(entity);
        log.info("User id={} deleted email id={}", userId, emailId);
    }

    // ---------- телефон ----------

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#userId"),
            @CacheEvict(value = "userSearch", allEntries = true)})
    public Long addPhone(Long userId, String phone) {
        ensurePhoneFree(phone);
        User user = requireUser(userId);
        PhoneData entity = PhoneData.builder().user(user).phone(phone).build();
        Long id = phoneRepository.save(entity).getId();
        log.info("User id={} added phone id={}", userId, id);
        return id;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#userId"),
            @CacheEvict(value = "userSearch", allEntries = true)})
    public void changePhone(Long userId, Long phoneId, String newPhone) {
        PhoneData entity = phoneRepository.findById(phoneId)
                .orElseThrow(() -> new NotFoundException("Phone not found: " + phoneId));
        requireOwnership(entity.getUser().getId(), userId, "phone");
        if (!entity.getPhone().equals(newPhone)) {
            ensurePhoneFree(newPhone);
            entity.setPhone(newPhone);
        }
        log.info("User id={} changed phone id={}", userId, phoneId);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#userId"),
            @CacheEvict(value = "userSearch", allEntries = true)})
    public void deletePhone(Long userId, Long phoneId) {
        PhoneData entity = phoneRepository.findById(phoneId)
                .orElseThrow(() -> new NotFoundException("Phone not found: " + phoneId));
        requireOwnership(entity.getUser().getId(), userId, "phone");
        if (phoneRepository.countByUserId(userId) <= 1) {
            throw new BusinessException("A user must keep at least one phone");
        }
        phoneRepository.delete(entity);
        log.info("User id={} deleted phone id={}", userId, phoneId);
    }

    // ---------- вспомогательные методы ----------

    private void ensureEmailFree(String email) {
        if (emailRepository.existsByEmail(email)) {
            throw new ConflictException("Email already in use: " + email);
        }
    }

    private void ensurePhoneFree(String phone) {
        if (phoneRepository.existsByPhone(phone)) {
            throw new ConflictException("Phone already in use: " + phone);
        }
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private void requireOwnership(Long ownerId, Long currentUserId, String what) {
        if (!ownerId.equals(currentUserId)) {
            // Скрываем существование чужих записей за 404.
            throw new NotFoundException(what + " not found");
        }
    }
}
