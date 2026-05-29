package home.interviewtask.userbank.api.exception;

/** Конфликт уникальности, например email/телефон уже используется другим пользователем. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
