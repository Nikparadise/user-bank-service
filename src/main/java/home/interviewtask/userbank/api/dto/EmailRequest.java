package home.interviewtask.userbank.api.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class EmailRequest {

    @NotBlank(message = "email не должен быть пустым")
    @Email(message = "email должен быть корректным адресом")
    @Size(max = 200, message = "email должен быть не длиннее 200 символов")
    private String email;
}
