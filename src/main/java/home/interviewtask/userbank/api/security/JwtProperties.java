package home.interviewtask.userbank.api.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** Секрет подписи в Base64 (минимум 256 бит для HS256). */
    private String secret;
    private long expirationMs = 3_600_000L;
}
