package com.emergency.system.service;

import com.emergency.system.dto.CreateUserRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.Console;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(name = "app.admin-recovery.enabled", havingValue = "true")
public class LocalAdminRecoveryRunner implements ApplicationRunner {
    private final AuthService authService;
    private final Validator validator;

    public LocalAdminRecoveryRunner(AuthService authService, Validator validator) {
        this.authService = authService;
        this.validator = validator;
    }

    @Override
    public void run(ApplicationArguments args) {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("Run local administrator recovery from an interactive terminal.");
        }

        String name = console.readLine("New administrator name: ");
        String email = console.readLine("New administrator email: ");
        char[] password = console.readPassword("New administrator password (12-72 characters): ");
        char[] confirmation = console.readPassword("Confirm password: ");
        try {
            if (password == null || confirmation == null
                    || !java.util.Arrays.equals(password, confirmation)) {
                throw new IllegalArgumentException("Passwords did not match.");
            }
            if (password.length < 12 || password.length > 72) {
                throw new IllegalArgumentException("Password must be between 12 and 72 characters.");
            }

            CreateUserRequest request = new CreateUserRequest(
                    name == null ? "" : name.trim(),
                    email == null ? "" : email.trim(),
                    new String(password),
                    "ADMIN");
            Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                String errors = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .sorted()
                        .collect(Collectors.joining(" "));
                throw new IllegalArgumentException("Invalid account details: " + errors);
            }

            authService.createManagedUser(request);
            console.printf("Created administrator account for %s.%n", request.email());
        } finally {
            if (password != null) {
                java.util.Arrays.fill(password, '\0');
            }
            if (confirmation != null) {
                java.util.Arrays.fill(confirmation, '\0');
            }
        }
    }
}
