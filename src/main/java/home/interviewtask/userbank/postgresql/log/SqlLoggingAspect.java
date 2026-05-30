package home.interviewtask.userbank.postgresql.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.stream.IntStream;

/**
 * Аспект для логирования вызовов методов SQL-репозиториев с их аргументами.
 * Перед каждым вызовом метода репозитория пишет в лог (уровень debug) две строки:
 * сначала переданные аргументы, затем сам запрос ({@code @Query}, если задан,
 * иначе запрос генерируется Spring Data/Hibernate по имени метода).
 */
@Aspect
@Component
@Slf4j(topic = "SqlLoggingAspect")
public class SqlLoggingAspect {

    @Before("execution(* home.interviewtask.userbank.postgresql.repository.*.*(..))")
    public void logSqlExecuteParametersMethod(JoinPoint joinPoint) {
        MethodSignature methodSig = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = methodSig.getParameterNames();
        Object[] args = joinPoint.getArgs();

        String methodSignature = methodSig.toShortString();
        // Сначала — переданные аргументы, ниже — сам запрос.
        log.debug("Метод {} вызван с аргументами: {}", methodSignature, logArguments(parameterNames, args));
        log.debug("Запрос: {}", resolveQuery(methodSig.getMethod()));
    }

    /**
     * Возвращает текст запроса: значение аннотации {@code @Query}, если она задана на методе,
     * иначе — пометку, что запрос генерируется по имени метода (фактический SQL см. в логах
     * {@code org.hibernate.SQL} на уровне debug).
     */
    private String resolveQuery(Method method) {
        Query query = method.getAnnotation(Query.class);
        if (query != null && !query.value().isEmpty()) {
            return (query.nativeQuery() ? "[native] " : "[JPQL] ") + query.value();
        }
        return "генерируется по имени метода (фактический SQL см. в логах org.hibernate.SQL)";
    }

    private String logArguments(String[] parameterNames, Object[] args) {
        if (args.length == 0) {
            return "без аргументов";
        }

        StringBuilder result = new StringBuilder();
        IntStream.range(0, args.length)
                .forEach(i -> {
                    if (i > 0) {
                        result.append(", ");
                    }
                    // Имена параметров доступны, если включён компиляторный флаг -parameters
                    // (его задаёт spring-boot-starter-parent); иначе подставляем argN.
                    String name = (parameterNames != null && i < parameterNames.length)
                            ? parameterNames[i] : "arg" + i;
                    result.append(name)
                            .append(": ")
                            .append(args[i]);
                });
        return result.toString();
    }
}
