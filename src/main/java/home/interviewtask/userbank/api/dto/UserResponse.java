package home.interviewtask.userbank.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Кэшируемый профиль пользователя. Баланс намеренно исключён — он меняется каждые 30 секунд
 * при начислении, поэтому отдаётся актуальным через {@code GET /api/accounts/me}.
 */
@Data
@Builder
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private List<String> emails;
    private List<String> phones;
}
