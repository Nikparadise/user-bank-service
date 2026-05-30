/*
 * Copyright (C) 2026 Milkov Nikita. All rights reserved.
 *
 * Organisation: home
 * Developer:    Milkov Nikita
 * Email:        nikparadise@mail.ru
 * Mobile:       8-920-130-6265
 *
 * Created:      29.05.2026
 * Last edited:  30.05.2026 15:05
 * Edited by:    Milkov Nikita
 */
package home.interviewtask.userbank.postgresql.repository;

import home.interviewtask.userbank.postgresql.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUserId(Long userId);

    /**
     * Пессимистичная блокировка на запись для перевода денег: строка блокируется
     * (SELECT ... FOR UPDATE), поэтому конкурентные переводы/начисления выполняются по очереди.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.user.id = :userId")
    Optional<Account> findByUserIdForUpdate(@Param("userId") Long userId);

    /** Счета, ещё не достигшие потолка начисления (balance &lt; initial * multiplier). */
    @Query("select a from Account a where a.balance < a.initialBalance * :maxMultiplier")
    List<Account> findAllBelowCap(@Param("maxMultiplier") java.math.BigDecimal maxMultiplier);
}
