package com.aoc.zerodowntime.service;

import com.aoc.zerodowntime.display.TestStatusDisplay;
import com.aoc.zerodowntime.model.ServiceType;
import com.aoc.zerodowntime.model.TestResult;
import com.aoc.zerodowntime.model.TestSummary;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public abstract class AbstractServiceTester implements ServiceTester {

    private final List<TestResult> results = Collections.synchronizedList(new ArrayList<>());
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final Map<Integer, AtomicInteger> statusCodeCounts = new ConcurrentHashMap<>();
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxResponseTime = new AtomicLong(0);
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    
    private ScheduledExecutorService executorService;
    private LocalDateTime startTime;
    private CompletableFuture<TestSummary> testFuture;
    private TestStatusDisplay statusDisplay;
    private int totalRequestCount = 0;

    protected abstract ServiceType getServiceType();
    
    /**
     * Set the status display for this tester
     * @param statusDisplay the status display to use
     */
    public void setStatusDisplay(TestStatusDisplay statusDisplay) {
        this.statusDisplay = statusDisplay;
    }

    @Override
    public CompletableFuture<TestSummary> startContinuousTest(long intervalMs) {
        if (running.compareAndSet(false, true)) {
            log.info("Starting continuous test for {} service with interval {}ms", getServiceName(), intervalMs);
            results.clear();
            statusCodeCounts.clear();
            successCount.set(0);
            failureCount.set(0);
            minResponseTime.set(Long.MAX_VALUE);
            maxResponseTime.set(0);
            totalResponseTime.set(0);
            totalRequestCount = 0;
            
            startTime = LocalDateTime.now();
            executorService = Executors.newSingleThreadScheduledExecutor();
            testFuture = new CompletableFuture<>();
            
            executorService.scheduleAtFixedRate(() -> {
                try {
                    TestResult result = executeTest();
                    results.add(result);
                    totalRequestCount++;
                    
                    // Update statistics
                    if (result.isSuccess()) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                    
                    statusCodeCounts.computeIfAbsent(result.getStatusCode(), k -> new AtomicInteger(0))
                            .incrementAndGet();
                    
                    long responseTime = result.getResponseTimeMs();
                    updateMinResponseTime(responseTime);
                    updateMaxResponseTime(responseTime);
                    totalResponseTime.addAndGet(responseTime);
                    
                    // Update the status display instead of logging each result
                    if (statusDisplay != null) {
                        statusDisplay.updateStatus(getServiceName(), this);
                    } else {
                        // Fallback to debug logging if status display is not available
                        log.debug("{} test result: status={}, success={}, time={}ms", 
                                getServiceName(), result.getStatusCode(), result.isSuccess(), responseTime);
                    }
                } catch (Exception e) {
                    log.error("Error executing test for {}", getServiceName(), e);
                    TestResult errorResult = TestResult.builder()
                            .serviceName(getServiceName())
                            .serviceType(getServiceType())
                            .timestamp(LocalDateTime.now())
                            .success(false)
                            .errorMessage(e.getMessage())
                            .build();
                    results.add(errorResult);
                    failureCount.incrementAndGet();
                    totalRequestCount++;
                }
            }, 0, intervalMs, TimeUnit.MILLISECONDS);
            
            return testFuture;
        } else {
            log.warn("Test for {} is already running", getServiceName());
            return CompletableFuture.completedFuture(null);
        }
    }
    
    private void updateMinResponseTime(long responseTime) {
        long currentMin = minResponseTime.get();
        while (responseTime < currentMin) {
            if (minResponseTime.compareAndSet(currentMin, responseTime)) {
                break;
            }
            currentMin = minResponseTime.get();
        }
    }
    
    private void updateMaxResponseTime(long responseTime) {
        long currentMax = maxResponseTime.get();
        while (responseTime > currentMax) {
            if (maxResponseTime.compareAndSet(currentMax, responseTime)) {
                break;
            }
            currentMax = maxResponseTime.get();
        }
    }

    @Override
    public void stopContinuousTest() {
        if (running.compareAndSet(true, false)) {
            log.info("Stopping continuous test for {}", getServiceName());
            executorService.shutdown();
            try {
                executorService.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for executor service to terminate", e);
                Thread.currentThread().interrupt();
            }
            
            // Clear status display when stopping the test
            if (statusDisplay != null) {
                statusDisplay.clearStatus(getServiceName());
            }
            
            TestSummary summary = generateSummary();
            testFuture.complete(summary);
        } else {
            log.warn("No test running for {}", getServiceName());
        }
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public List<TestResult> getResults() {
        return Collections.unmodifiableList(results);
    }
    
    @Override
    public Map<Integer, Integer> getStatusCodeCounts() {
        Map<Integer, Integer> counts = new ConcurrentHashMap<>();
        statusCodeCounts.forEach((code, count) -> counts.put(code, count.get()));
        return counts;
    }
    
    @Override
    public int getSuccessCount() {
        return successCount.get();
    }
    
    @Override
    public int getFailureCount() {
        return failureCount.get();
    }
    
    @Override
    public long getMinResponseTime() {
        return minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get();
    }
    
    @Override
    public long getMaxResponseTime() {
        return maxResponseTime.get();
    }
    
    @Override
    public double getAvgResponseTime() {
        int totalRequests = results.size();
        return totalRequests > 0 ? (double) totalResponseTime.get() / totalRequests : 0;
    }
    
    @Override
    public Duration getElapsedTime() {
        return Duration.between(startTime != null ? startTime : LocalDateTime.now(), LocalDateTime.now());
    }

    @Override
    public TestSummary generateSummary() {
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime != null ? startTime : endTime, endTime);
        
        int totalRequests = results.size();
        int successfulRequests = successCount.get();
        int failedRequests = failureCount.get();
        double successRate = totalRequests > 0 ? (double) successfulRequests / totalRequests * 100 : 0;
        
        long min = getMinResponseTime();
        long max = getMaxResponseTime();
        double avg = getAvgResponseTime();
        
        return TestSummary.builder()
                .serviceName(getServiceName())
                .serviceType(getServiceType())
                .startTime(startTime)
                .endTime(endTime)
                .totalDuration(duration)
                .totalRequests(totalRequests)
                .successfulRequests(successfulRequests)
                .failedRequests(failedRequests)
                .successRate(successRate)
                .minResponseTime(min)
                .maxResponseTime(max)
                .avgResponseTime(avg)
                .results(new ArrayList<>(results))
                .build();
    }
}
