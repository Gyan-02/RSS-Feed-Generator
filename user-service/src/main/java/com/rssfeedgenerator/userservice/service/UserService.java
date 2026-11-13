package com.rssfeedgenerator.userservice.service;

import com.rssfeedgenerator.userservice.dto.AdminUserResponse;
import com.rssfeedgenerator.userservice.dto.LoginRequest;
import com.rssfeedgenerator.userservice.dto.RegisterRequest;
import com.rssfeedgenerator.userservice.dto.UpdateProfileRequest;
import com.rssfeedgenerator.userservice.dto.UserResponse;
import com.rssfeedgenerator.userservice.exception.DuplicateEmailException;
import com.rssfeedgenerator.userservice.exception.InvalidCredentialsException;
import com.rssfeedgenerator.userservice.exception.UserNotFoundException;
import com.rssfeedgenerator.userservice.model.AppUser;
import com.rssfeedgenerator.userservice.model.SignInEvent;
import com.rssfeedgenerator.userservice.repository.SignInEventRepository;
import com.rssfeedgenerator.userservice.repository.UserRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final SignInEventRepository signInEventRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            SignInEventRepository signInEventRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.signInEventRepository = signInEventRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAdminUsers() {
        return userRepository.findAll().stream()
                .map(user -> new AdminUserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRegisteredAt(),
                        signInEventRepository.findByUserIdOrderBySignedInAtDesc(user.getId())
                                .stream()
                                .map(event -> event.getSignedInAt())
                                .toList()))
                .toList();
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException();
        }

        AppUser user = new AppUser(
                request.name().trim(),
                email,
                passwordEncoder.encode(request.password())
        );
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        signInEventRepository.save(new SignInEvent(user));
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long id, UpdateProfileRequest request) {
        String email = normalizeEmail(request.email());
        AppUser user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateEmailException();
        }
        user.setName(request.name().trim());
        user.setEmail(email);
        return toResponse(userRepository.save(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
