package home.interviewtask.userbank.api.controller;

import home.interviewtask.userbank.api.dto.PageResponse;
import home.interviewtask.userbank.api.dto.UserResponse;
import home.interviewtask.userbank.api.dto.UserSearchRequest;
import home.interviewtask.userbank.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ModelAttribute;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "Получить профиль пользователя по id")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    @GetMapping
    @Operation(summary = "Поиск пользователей с фильтрами и пагинацией (size, page)")
    public PageResponse<UserResponse> search(@ModelAttribute UserSearchRequest request,
                                             @PageableDefault(size = 20) Pageable pageable) {
        return userService.search(request, pageable);
    }
}
