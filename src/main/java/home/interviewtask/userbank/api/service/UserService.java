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

import home.interviewtask.userbank.api.dto.PageResponse;
import home.interviewtask.userbank.api.dto.UserResponse;
import home.interviewtask.userbank.api.dto.UserSearchRequest;
import home.interviewtask.userbank.postgresql.entity.User;
import home.interviewtask.userbank.api.exception.NotFoundException;
import home.interviewtask.userbank.api.mapper.UserMapper;
import home.interviewtask.userbank.postgresql.repository.UserRepository;
import home.interviewtask.userbank.postgresql.repository.UserSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#userId")
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "userSearch",
            key = "T(java.util.Objects).hash(#request.dateOfBirth, #request.phone, #request.name, "
                    + "#request.email, #pageable.pageNumber, #pageable.pageSize)")
    public PageResponse<UserResponse> search(UserSearchRequest request, Pageable pageable) {
        List<Specification<User>> specs = new ArrayList<>();
        if (request.getDateOfBirth() != null) {
            specs.add(UserSpecifications.dateOfBirthAfter(request.getDateOfBirth()));
        }
        if (StringUtils.hasText(request.getPhone())) {
            specs.add(UserSpecifications.hasPhone(request.getPhone()));
        }
        if (StringUtils.hasText(request.getName())) {
            specs.add(UserSpecifications.nameStartsWith(request.getName()));
        }
        if (StringUtils.hasText(request.getEmail())) {
            specs.add(UserSpecifications.hasEmail(request.getEmail()));
        }

        Specification<User> combined = specs.stream().reduce(Specification::and).orElse(null);
        Page<User> page = userRepository.findAll(combined, pageable);
        log.debug("User search returned {} of {} records", page.getNumberOfElements(), page.getTotalElements());
        return PageResponse.from(page.map(userMapper::toResponse));
    }
}
