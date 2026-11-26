package com.emergency.system.service;

import com.emergency.system.entity.User;
import com.emergency.system.repository.UserRepository;
import com.emergency.system.dto.CreateUserRequest;
import com.emergency.system.dto.PublicUser;
import com.emergency.system.dto.RegisterRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PublicUser register(RegisterRequest request) {
        String role = normalizeRole(request.role());
        if (!Set.of("STUDENT", "FACULTY").contains(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only students and faculty can self-register. Ask an administrator to create staff accounts.");
        }
        return createUser(request.name(), request.email(), request.password(), role);
    }

    public PublicUser bootstrapAdministrator(RegisterRequest request) {
        if (userRepository.count() != 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Initial setup has already been completed.");
        }
        return createUser(request.name(), request.email(), request.password(), "ADMIN");
    }

    public PublicUser createManagedUser(CreateUserRequest request) {
        String role = normalizeRole(request.role());
        if (!Set.of("STUDENT", "FACULTY", "GUARD", "SECURITY", "ADMIN").contains(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported account role.");
        }
        return createUser(request.name(), request.email(), request.password(), role);
    }

    public PublicUser toPublicUser(User user) {
        return new PublicUser(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public void upgradeLegacyPassword(User user, String rawPassword) {
        if (passwordEncoder instanceof com.emergency.system.config.LegacyAwarePasswordEncoder encoder
                && encoder.isLegacy(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
        }
    }

    private PublicUser createUser(String name, String email, String password, String role) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists.");
        }
        User user = new User();
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        return toPublicUser(userRepository.save(user));
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
    }
}
