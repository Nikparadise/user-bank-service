package home.interviewtask.userbank.exception;

/**
 * Бросается при нарушении бизнес-правил (например, недостаточно средств, удаление последнего
 * email/телефона, значение занято другим пользователем). Маппится на HTTP 400/409.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
