package home.interviewtask.userbank.api.mapper;

import home.interviewtask.userbank.api.dto.UserResponse;
import home.interviewtask.userbank.postgresql.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        List<String> emails = user.getEmails().stream()
                .map(e -> e.getEmail())
                .collect(Collectors.toList());
        List<String> phones = user.getPhones().stream()
                .map(p -> p.getPhone())
                .collect(Collectors.toList());

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .dateOfBirth(user.getDateOfBirth())
                .emails(emails)
                .phones(phones)
                .build();
    }
}
