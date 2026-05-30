package home.interviewtask.userbank.scheduler;

import home.interviewtask.userbank.service.BalanceAccrualService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BalanceAccrualScheduler {

    private final BalanceAccrualService accrualService;

    /**
     * Запускается каждые {@code app.balance.scheduler-fixed-rate-ms} (по умолчанию 30с).
     * Каждый счёт начисляется в своей короткой транзакции с блокировкой, поэтому медленный/сбойный
     * счёт не блокирует остальные и никогда не конкурирует с одновременным переводом.
     */
    @Scheduled(fixedRateString = "${app.balance.scheduler-fixed-rate-ms:30000}",
            initialDelayString = "${app.balance.scheduler-fixed-rate-ms:30000}")
    public void accrueBalances() {
        List<Long> userIds = accrualService.findUserIdsBelowCap();
        if (userIds.isEmpty()) {
            return;
        }
        int updated = 0;
        for (Long userId : userIds) {
            try {
                accrualService.accrueOne(userId);
                updated++;
            } catch (Exception ex) {
                log.warn("Balance accrual failed for user id={}: {}", userId, ex.getMessage());
            }
        }
        log.info("Balance accrual cycle finished: {} account(s) processed", updated);
    }
}
