package home.interviewtask.userbank.api.service;

import home.interviewtask.userbank.api.dto.AccountResponse;
import home.interviewtask.userbank.postgresql.entity.Account;
import home.interviewtask.userbank.api.exception.NotFoundException;
import home.interviewtask.userbank.postgresql.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    /** Всегда отдаётся актуальным (без кэша), так как баланс меняется каждые 30 секунд. */
    @Transactional(readOnly = true)
    public AccountResponse getBalance(Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Account not found for user: " + userId));
        return new AccountResponse(userId, account.getBalance(), account.getInitialBalance());
    }
}
