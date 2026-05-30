package home.interviewtask.userbank.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AccountResponse {

    private Long userId;
    private BigDecimal balance;
    private BigDecimal initialBalance;
}
