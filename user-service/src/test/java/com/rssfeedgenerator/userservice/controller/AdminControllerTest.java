package com.rssfeedgenerator.userservice.controller;

import com.rssfeedgenerator.userservice.dto.AdminUserResponse;
import com.rssfeedgenerator.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdminControllerTest {

    @Test
    void unconfiguredAdminKeyReturnsServiceUnavailableWithoutReadingUsers() {
        var service = new RecordingUserService();
        var controller = new AdminController(service, " ");

        var response = controller.getRegisteredUsers("any-key");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(service.readCount).isZero();
    }

    @Test
    void missingAdminKeyReturnsUnauthorized() {
        var service = new RecordingUserService();
        var controller = new AdminController(service, "configured-key");

        assertThat(controller.getRegisteredUsers(null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(service.readCount).isZero();
    }

    @Test
    void incorrectAdminKeyReturnsUnauthorized() {
        var service = new RecordingUserService();
        var controller = new AdminController(service, "configured-key");

        assertThat(controller.getRegisteredUsers("wrong-key").getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(service.readCount).isZero();
    }

    @Test
    void correctAdminKeyReturnsAuditRecords() {
        var service = new RecordingUserService();
        var controller = new AdminController(service, "configured-key");
        var expected = List.of(new AdminUserResponse(3L, "Gyan Gaurav", "gyan@example.com",
                Instant.parse("2026-10-01T10:00:00Z"), List.of(Instant.parse("2026-11-13T09:00:00Z"))));
        service.records = expected;

        var response = controller.getRegisteredUsers("configured-key");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        assertThat(service.readCount).isEqualTo(1);
    }

    private static final class RecordingUserService extends UserService {
        private int readCount;
        private List<AdminUserResponse> records = List.of();

        private RecordingUserService() { super(null, null, null); }

        @Override
        public List<AdminUserResponse> getAdminUsers() {
            readCount++;
            return records;
        }
    }
}
