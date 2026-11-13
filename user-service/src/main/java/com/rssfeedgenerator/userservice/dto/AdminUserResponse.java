package com.rssfeedgenerator.userservice.dto;

import java.time.Instant;
import java.util.List;

public record AdminUserResponse(
        Long id,
        String name,
        String email,
        Instant registeredAt,
        List<Instant> signIns) {
}
