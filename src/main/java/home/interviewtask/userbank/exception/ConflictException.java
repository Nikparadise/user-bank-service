package home.interviewtask.userbank.exception;

/** Конфликт уникальности, например email/телефон уже используется другим пользователем. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
