package home.interviewtask.userbank.api.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Необязательные фильтры поиска. Можно передать любую комбинацию; отсутствующие поля игнорируются.
 */
@Data
public class UserSearchRequest {

    /** Записи, у которых date_of_birth строго больше переданного значения. */
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate dateOfBirth;

    /** Точное совпадение (100%). */
    private String phone;

    /** LIKE '{name}%'. */
    private String name;

    /** Точное совпадение (100%). */
    private String email;
}
