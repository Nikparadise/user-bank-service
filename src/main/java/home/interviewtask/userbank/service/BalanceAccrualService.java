package home.interviewtask.userbank.service;

import home.interviewtask.userbank.config.BalanceProperties;
import home.interviewtask.userbank.postgresql.entity.Account;
import home.interviewtask.userbank.postgresql.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Выполняет периодическое начисление на баланс: за каждый цикл баланс растёт на {@code accrualRate}
 * (по умолчанию +10%), но никогда не превышает {@code maxMultiplier} от первоначального депозита (по умолчанию 207%).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceAccrualService {

    private final AccountRepository accountRepository;
    private final BalanceProperties properties;

    /** Находит счета, ещё не достигшие потолка. Только чтение, без блокировок. */
    @Transactional(readOnly = true)
    public List<Long> findUserIdsBelowCap() {
        return accountRepository.findAllBelowCap(properties.getMaxMultiplier()).stream()
                .map(a -> a.getUser().getId())
                .collect(Collectors.toList());
    }

    /**
     * Начисляет на один счёт под пессимистичной блокировкой, чтобы не конкурировать с переводом.
     * Каждый счёт обрабатывается в своей короткой транзакции.
     */
    @Transactional
    public void accrueOne(Long userId) {
        Account account = accountRepository.findByUserIdForUpdate(userId).orElse(null);
        if (account == null) {
            return;
        }
        BigDecimal cap = account.getInitialBalance()
                .multiply(properties.getMaxMultiplier())
                .setScale(2, RoundingMode.HALF_UP);
        if (account.getBalance().compareTo(cap) >= 0) {
            return;
        }
        BigDecimal grown = account.getBalance()
                .multiply(BigDecimal.ONE.add(properties.getAccrualRate()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal newBalance = grown.min(cap);
        account.setBalance(newBalance);
    }
}
