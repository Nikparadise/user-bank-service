package home.interviewtask.userbank.api.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotNull(message = "toUserId обязателен")
    private Long toUserId;

    @NotNull(message = "value обязателен")
    @DecimalMin(value = "0.01", message = "value должен быть положительным")
    @Digits(integer = 17, fraction = 2, message = "value должен иметь не более 2 знаков после запятой")
    private BigDecimal value;
}
