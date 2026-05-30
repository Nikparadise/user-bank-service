package home.interviewtask.userbank.postgresql.repository;

import home.interviewtask.userbank.postgresql.entity.User;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import java.time.LocalDate;

/**
 * Спецификации для поиска пользователей:
 *  - dateOfBirth : записи, у которых date_of_birth строго больше значения
 *  - phone       : точное совпадение (100%)
 *  - name        : LIKE '{name}%'
 *  - email       : точное совпадение (100%)
 */
public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> dateOfBirthAfter(LocalDate dateOfBirth) {
        return (root, query, cb) ->
                cb.greaterThan(root.get("dateOfBirth"), dateOfBirth);
    }

    public static Specification<User> nameStartsWith(String name) {
        return (root, query, cb) ->
                cb.like(root.get("name"), name + "%");
    }

    public static Specification<User> hasPhone(String phone) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Object, Object> phones = root.join("phones");
            return cb.equal(phones.get("phone"), phone);
        };
    }

    public static Specification<User> hasEmail(String email) {
        return (root, query, cb) -> {
            query.distinct(true);
            Join<Object, Object> emails = root.join("emails");
            return cb.equal(emails.get("email"), email);
        };
    }
}
