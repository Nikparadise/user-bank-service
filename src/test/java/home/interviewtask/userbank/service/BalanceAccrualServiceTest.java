package home.interviewtask.userbank.service;

import home.interviewtask.userbank.config.BalanceProperties;
import home.interviewtask.userbank.postgresql.entity.Account;
import home.interviewtask.userbank.postgresql.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceAccrualServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private BalanceAccrualService service;

    @BeforeEach
    void setUp() {
        service = new BalanceAccrualService(accountRepository, new BalanceProperties());
    }

    private Account account(String balance, String initial) {
        return Account.builder()
                .balance(new BigDecimal(balance))
                .initialBalance(new BigDecimal(initial))
                .build();
    }

    @Test
    void addsTenPercent() {
        Account acc = account("100.00", "100.00");
        when(accountRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(acc));

        service.accrueOne(1L);

        assertThat(acc.getBalance()).isEqualByComparingTo("110.00");
    }

    @Test
    void neverExceeds207PercentOfInitialDeposit() {
        // 200 — это уже 200% от 100; +10% дало бы 220, но потолок 207
        Account acc = account("200.00", "100.00");
        when(accountRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(acc));

        service.accrueOne(1L);

        assertThat(acc.getBalance()).isEqualByComparingTo("207.00");
    }

    @Test
    void doesNotGrowOnceCapReached() {
        Account acc = account("207.00", "100.00");
        when(accountRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(acc));

        service.accrueOne(1L);

        assertThat(acc.getBalance()).isEqualByComparingTo("207.00");
    }
}
