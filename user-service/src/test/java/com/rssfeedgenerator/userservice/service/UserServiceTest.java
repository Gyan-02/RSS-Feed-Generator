package com.rssfeedgenerator.userservice.service;

import com.rssfeedgenerator.userservice.dto.LoginRequest;
import com.rssfeedgenerator.userservice.dto.RegisterRequest;
import com.rssfeedgenerator.userservice.dto.UpdateProfileRequest;
import com.rssfeedgenerator.userservice.exception.DuplicateEmailException;
import com.rssfeedgenerator.userservice.exception.InvalidCredentialsException;
import com.rssfeedgenerator.userservice.exception.UserNotFoundException;
import com.rssfeedgenerator.userservice.support.InMemoryRepositories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest {
    private InMemoryRepositories store;
    private UserService service;

    @BeforeEach
    void setUp() {
        store = new InMemoryRepositories();
        service = new UserService(store.users(), store.signIns(), testEncoder());
    }

    @Test
    void registerTrimsNameNormalizesEmailAndReturnsSafeProfile() {
        var response = service.register(new RegisterRequest("  Gyan Gaurav ", " GYAN@EXAMPLE.COM ", "Password123!"));
        var saved = store.users().findByEmail("gyan@example.com").orElseThrow();

        assertThat(response.name()).isEqualTo("Gyan Gaurav");
        assertThat(response.email()).isEqualTo("gyan@example.com");
        assertThat(saved.getPasswordHash()).startsWith("encoded:").isNotEqualTo("Password123!");
        assertThat(response.toString()).doesNotContain("Password123!", "encoded:");
        assertThat(saved.getRegisteredAt()).isNotNull();
    }

    @Test
    void registrationRejectsDuplicateEmailRegardlessOfCaseAndWhitespace() {
        service.register(new RegisterRequest("Gyan", "gyan@example.com", "Password123!"));

        assertThatThrownBy(() -> service.register(new RegisterRequest("Other", " GYAN@EXAMPLE.COM ", "Password456!")))
                .isInstanceOf(DuplicateEmailException.class);
        assertThat(store.users().findAll()).hasSize(1);
    }

    @Test
    void successfulLoginNormalizesEmailAndRecordsOneSignIn() {
        service.register(new RegisterRequest("Gyan", "gyan@example.com", "Password123!"));

        var response = service.login(new LoginRequest(" GYAN@EXAMPLE.COM ", "Password123!"));
        var admin = service.getAdminUsers().get(0);

        assertThat(response.email()).isEqualTo("gyan@example.com");
        assertThat(admin.signIns()).hasSize(1);
        assertThat(admin.signIns().get(0)).isNotNull();
    }

    @Test
    void unknownEmailLoginFailsAndDoesNotRecordSignIn() {
        assertThatThrownBy(() -> service.login(new LoginRequest("missing@example.com", "Password123!")))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThat(service.getAdminUsers()).isEmpty();
    }

    @Test
    void incorrectPasswordFailsAndDoesNotRecordSignIn() {
        service.register(new RegisterRequest("Gyan", "gyan@example.com", "Password123!"));

        assertThatThrownBy(() -> service.login(new LoginRequest("gyan@example.com", "Incorrect123!")))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThat(service.getAdminUsers().get(0).signIns()).isEmpty();
    }

    @Test
    void profileUpdateNormalizesEmailAfterPasswordConfirmation() {
        var created = service.register(new RegisterRequest("Gyan", "gyan@example.com", "Password123!"));

        var updated = service.updateProfile(created.id(), new UpdateProfileRequest(" Gyan G. ", " NEW@EXAMPLE.COM ", "Password123!"));

        assertThat(updated.name()).isEqualTo("Gyan G.");
        assertThat(updated.email()).isEqualTo("new@example.com");
        assertThat(store.users().findById(created.id()).orElseThrow().getPasswordHash()).startsWith("encoded:");
    }

    @Test
    void profileUpdateRejectsIncorrectCurrentPassword() {
        var created = service.register(new RegisterRequest("Gyan", "gyan@example.com", "Password123!"));

        assertThatThrownBy(() -> service.updateProfile(created.id(), new UpdateProfileRequest("Changed", "new@example.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThat(store.users().findById(created.id()).orElseThrow().getEmail()).isEqualTo("gyan@example.com");
    }

    @Test
    void profileUpdateRejectsAnEmailAlreadyOwnedByAnotherAccount() {
        var first = service.register(new RegisterRequest("First", "first@example.com", "Password123!"));
        service.register(new RegisterRequest("Second", "second@example.com", "Password123!"));

        assertThatThrownBy(() -> service.updateProfile(first.id(), new UpdateProfileRequest("First", "second@example.com", "Password123!")))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void profileUpdateReturnsNotFoundForMissingAccount() {
        assertThatThrownBy(() -> service.updateProfile(404L, new UpdateProfileRequest("Name", "n@example.com", "Password123!")))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void adminRecordsContainRegistrationAndSuccessfulSignInTimesButNoPasswordHash() {
        service.register(new RegisterRequest("Gyan", "gyan@example.com", "Password123!"));
        service.login(new LoginRequest("gyan@example.com", "Password123!"));
        service.login(new LoginRequest("gyan@example.com", "Password123!"));

        var record = service.getAdminUsers().get(0);

        assertThat(record.registeredAt()).isNotNull();
        assertThat(record.signIns()).hasSize(2).allMatch(time -> time != null);
        assertThat(record.toString()).doesNotContain("passwordHash", "encoded:");
    }

    private static PasswordEncoder testEncoder() {
        return new PasswordEncoder() {
            @Override public String encode(CharSequence rawPassword) { return "encoded:" + rawPassword; }
            @Override public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return encodedPassword.equals(encode(rawPassword));
            }
        };
    }
}
