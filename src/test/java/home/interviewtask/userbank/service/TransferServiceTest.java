package home.interviewtask.userbank.service;

import home.interviewtask.userbank.api.service.TransferService;
import home.interviewtask.userbank.postgresql.entity.Account;
import home.interviewtask.userbank.api.exception.BusinessException;
import home.interviewtask.userbank.api.exception.NotFoundException;
import home.interviewtask.userbank.postgresql.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransferService transferService;

    private Account account(long userId, String balance) {
        return Account.builder()
                .id(userId)
                .balance(new BigDecimal(balance))
                .initialBalance(new BigDecimal(balance))
                .build();
    }

    @Test
    void transfersMoneyBetweenAccounts() {
        Account from = account(1L, "100.00");
        Account to = account(2L, "50.00");
        when(accountRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(from));
        when(accountRepository.findByUserIdForUpdate(2L)).thenReturn(Optional.of(to));

        transferService.transfer(1L, 2L, new BigDecimal("30.00"));

        assertThat(from.getBalance()).isEqualByComparingTo("70.00");
        assertThat(to.getBalance()).isEqualByComparingTo("80.00");
    }

    @Test
    void rejectsTransferWhenFundsAreInsufficient() {
        Account from = account(1L, "20.00");
        Account to = account(2L, "0.00");
        when(accountRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(from));
        when(accountRepository.findByUserIdForUpdate(2L)).thenReturn(Optional.of(to));

        assertThatThrownBy(() -> transferService.transfer(1L, 2L, new BigDecimal("50.00")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient funds");

        // балансы не изменились
        assertThat(from.getBalance()).isEqualByComparingTo("20.00");
        assertThat(to.getBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void rejectsSelfTransfer() {
        assertThatThrownBy(() -> transferService.transfer(1L, 1L, new BigDecimal("10.00")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("yourself");
        verify(accountRepository, never()).findByUserIdForUpdate(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void rejectsNonPositiveValue() {
        assertThatThrownBy(() -> transferService.transfer(1L, 2L, new BigDecimal("0.00")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("positive");
    }

    @Test
    void failsWhenRecipientAccountMissing() {
        Account from = account(1L, "100.00");
        // блокировки захватываются в порядке id: сначала 1, затем 2
        when(accountRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(from));
        when(accountRepository.findByUserIdForUpdate(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transfer(1L, 2L, new BigDecimal("10.00")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void acquiresLocksInAscendingUserIdOrderToAvoidDeadlock() {
        Account from = account(2L, "100.00");
        Account to = account(1L, "0.00");
        // перевод 2 -> 1, но меньший id (1) всё равно должен блокироваться первым
        lenient().when(accountRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(to));
        lenient().when(accountRepository.findByUserIdForUpdate(2L)).thenReturn(Optional.of(from));

        transferService.transfer(2L, 1L, new BigDecimal("40.00"));

        org.mockito.InOrder inOrder = org.mockito.Mockito.inOrder(accountRepository);
        inOrder.verify(accountRepository).findByUserIdForUpdate(1L);
        inOrder.verify(accountRepository).findByUserIdForUpdate(2L);

        assertThat(from.getBalance()).isEqualByComparingTo("60.00");
        assertThat(to.getBalance()).isEqualByComparingTo("40.00");
    }
}
