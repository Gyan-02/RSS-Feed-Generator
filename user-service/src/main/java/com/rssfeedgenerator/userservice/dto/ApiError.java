package com.rssfeedgenerator.userservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ApiError(LocalDateTime timestamp, int status, String error, List<String> messages) {
}
