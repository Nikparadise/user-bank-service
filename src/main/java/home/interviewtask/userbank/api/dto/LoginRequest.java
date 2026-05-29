package home.interviewtask.userbank.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * Аутентификация по email+пароль ЛИБО телефон+пароль.
 * Должно быть передано ровно одно из {@code email} / {@code phone} (проверяется в сервисе).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    private String email;
    private String phone;

    @NotBlank(message = "пароль не должен быть пустым")
    private String password;
}
