package com.aoc.zerodowntime.model;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TestSummary {
    private String serviceName;
    private ServiceType serviceType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration totalDuration;
    private int totalRequests;
    private int successfulRequests;
    private int failedRequests;
    private double successRate;
    private long minResponseTime;
    private long maxResponseTime;
    private double avgResponseTime;
    private List<TestResult> results;
}
