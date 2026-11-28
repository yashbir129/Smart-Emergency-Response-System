package com.emergency.system.controller;

import com.emergency.system.dto.CreateUserRequest;
import com.emergency.system.dto.PublicUser;
import com.emergency.system.repository.UserRepository;
import com.emergency.system.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final AuthService authService;
    private final UserRepository userRepository;

    public AdminUserController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<PublicUser> listUsers() {
        return userRepository.findAll().stream().map(authService::toPublicUser).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PublicUser createUser(@Valid @RequestBody CreateUserRequest request) {
        return authService.createManagedUser(request);
    }
}
