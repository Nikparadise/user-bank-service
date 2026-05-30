package home.interviewtask.userbank.api.controller;

import home.interviewtask.userbank.api.dto.LoginRequest;
import home.interviewtask.userbank.api.dto.TransferRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class UserApiIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // не даём планировщику начислений сработать во время теста
        registry.add("app.balance.scheduler-fixed-rate-ms", () -> "3600000");
        // тестовый секрет JWT — чтобы тест не зависел от некоммитимого secret.properties
        registry.add("app.jwt.secret", () -> "dGVzdC1zZWNyZXQta2V5LWZvci1qdW5pdC1pbnRlZ3JhdGlvbi0zMmI=");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String login(String email, String password) throws Exception {
        LoginRequest req = new LoginRequest(email, null, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("accessToken").asText();
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        LoginRequest req = new LoginRequest("nikparadise@mail.ru", null, "wrong-password");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void searchRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/users").param("name", "Americano"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void searchByNamePrefixReturnsMatchingUser() throws Exception {
        String token = login("nikparadise@mail.ru", "password1");

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .param("name", "Americano"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.content[0].name").value("Americano Bill Gates"))
                .andExpect(jsonPath("$.content[0].emails", org.hamcrest.Matchers.hasItem("test@icloud.com")));
    }

    @Test
    void searchByNamePrefixMatchesBothMilkovs() throws Exception {
        String token = login("nikparadise@mail.ru", "password1");

        // LIKE 'Мильков%' находит и «Мильков Никита», и «Милькова Александра»
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .param("name", "Мильков"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(2)));
    }

    @Test
    void transferMovesMoneyBetweenUsers() throws Exception {
        String fromToken = login("nikparadise@mail.ru", "password1");

        // у пользователя 1 изначально 100.00
        mockMvc.perform(get("/api/accounts/me").header("Authorization", "Bearer " + fromToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.00));

        TransferRequest transfer = new TransferRequest();
        transfer.setToUserId(2L);
        transfer.setValue(new BigDecimal("30.00"));

        mockMvc.perform(post("/api/accounts/transfer")
                        .header("Authorization", "Bearer " + fromToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isOk());

        // у пользователя 1 теперь 70.00
        mockMvc.perform(get("/api/accounts/me").header("Authorization", "Bearer " + fromToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(70.00));

        // у пользователя 2 теперь 280.50
        String toToken = login("sadovskaf@mail.ru", "password2");
        MvcResult to = mockMvc.perform(get("/api/accounts/me").header("Authorization", "Bearer " + toToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode toBalance = objectMapper.readTree(to.getResponse().getContentAsString());
        assertThat(new BigDecimal(toBalance.get("balance").asText())).isEqualByComparingTo("280.50");
    }

    @Test
    void transferRejectsInsufficientFunds() throws Exception {
        String fromToken = login("nikparadise@mail.ru", "password1");
        TransferRequest transfer = new TransferRequest();
        transfer.setToUserId(2L);
        transfer.setValue(new BigDecimal("999999.00"));

        mockMvc.perform(post("/api/accounts/transfer")
                        .header("Authorization", "Bearer " + fromToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transfer)))
                .andExpect(status().isBadRequest());
    }
}
