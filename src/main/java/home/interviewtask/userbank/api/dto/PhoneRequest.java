package home.interviewtask.userbank.api.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class PhoneRequest {

    /** Формат вида 79207865432 (11 цифр, начинается с 7). */
    @NotBlank(message = "телефон не должен быть пустым")
    @Pattern(regexp = "\\d{11,13}", message = "телефон должен состоять из 11-13 цифр, например 79207865432")
    private String phone;
}
