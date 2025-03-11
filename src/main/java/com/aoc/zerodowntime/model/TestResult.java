package com.aoc.zerodowntime.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TestResult {
    private String serviceName;
    private ServiceType serviceType;
    private LocalDateTime timestamp;
    private int statusCode;
    private boolean success;
    private long responseTimeMs;
    private String errorMessage;
}
