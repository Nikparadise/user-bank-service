package home.interviewtask.userbank.postgresql.repository;

import home.interviewtask.userbank.postgresql.entity.PhoneData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PhoneDataRepository extends JpaRepository<PhoneData, Long> {

    Optional<PhoneData> findByPhone(String phone);

    boolean existsByPhone(String phone);

    List<PhoneData> findAllByUserId(Long userId);

    long countByUserId(Long userId);
}
