package home.interviewtask.userbank.api.service;

import home.interviewtask.userbank.api.dto.LoginRequest;
import home.interviewtask.userbank.api.dto.TokenResponse;
import home.interviewtask.userbank.postgresql.entity.User;
import home.interviewtask.userbank.exception.BusinessException;
import home.interviewtask.userbank.postgresql.repository.UserRepository;
import home.interviewtask.userbank.api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    /**
     * Аутентифицирует по email+пароль ЛИБО телефон+пароль и выдаёт JWT.
     * При любой ошибке возвращается одинаковое сообщение, чтобы исключить перебор пользователей.
     */
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        boolean hasEmail = StringUtils.hasText(request.getEmail());
        boolean hasPhone = StringUtils.hasText(request.getPhone());
        if (hasEmail == hasPhone) {
            throw new BusinessException("Provide exactly one of: email or phone");
        }

        User user = (hasEmail
                ? userRepository.findByEmails_Email(request.getEmail())
                : userRepository.findByPhones_Phone(request.getPhone()))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.info("Failed login attempt for user id={}", user.getId());
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = tokenProvider.generateToken(user.getId());
        log.info("User id={} authenticated", user.getId());
        return new TokenResponse(token, "Bearer", tokenProvider.getExpirationMs());
    }
}
