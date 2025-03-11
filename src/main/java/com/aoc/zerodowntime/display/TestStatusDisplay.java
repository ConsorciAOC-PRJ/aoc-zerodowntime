package com.aoc.zerodowntime.display;

import com.aoc.zerodowntime.service.ServiceTester;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Handles the display of test status in a static format
 * rather than scrolling logs.
 */
@Slf4j
@Component
public class TestStatusDisplay {

    private final Map<String, Long> lastDisplayTime = new ConcurrentHashMap<>();
    private static final long DISPLAY_INTERVAL_MS = 2000; // Update display every 2 seconds

    /**
     * Update the status display for a service tester
     * @param serviceName The name of the service
     * @param tester The service tester instance
     */
    public void updateStatus(String serviceName, ServiceTester tester) {
        long now = System.currentTimeMillis();
        Long lastTime = lastDisplayTime.getOrDefault(serviceName, 0L);
        
        // Only update display at intervals to avoid too frequent updates
        if (now - lastTime >= DISPLAY_INTERVAL_MS) {
            lastDisplayTime.put(serviceName, now);
            displayStatus(serviceName, tester);
        }
    }
    
    /**
     * Display the current status of a test
     * @param serviceName The name of the service
     * @param tester The service tester instance
     */
    private void displayStatus(String serviceName, ServiceTester tester) {
        Duration elapsed = tester.getElapsedTime();
        String elapsedTime = String.format("%02d:%02d:%02d", 
                elapsed.toHours(), 
                elapsed.toMinutesPart(), 
                elapsed.toSecondsPart());
        
        int totalRequests = tester.getResults().size();
        int successCount = tester.getSuccessCount();
        int failureCount = tester.getFailureCount();
        double successRate = totalRequests > 0 ? 
                (double) successCount / totalRequests * 100 : 0;
        
        Map<Integer, Integer> statusCodes = tester.getStatusCodeCounts();
        String statusCodeSummary = statusCodes.isEmpty() ? "None" : 
                statusCodes.entrySet().stream()
                        .map(e -> e.getKey() + " (" + e.getValue() + ")")
                        .collect(Collectors.joining(", "));
        
        // Clear previous lines and print static summary
        StringBuilder summary = new StringBuilder();
        summary.append("\r"); // Return to start of line
        summary.append("=== ").append(serviceName).append(" Test Status ===\n");
        summary.append("Elapsed: ").append(elapsedTime);
        summary.append(" | Requests: ").append(totalRequests);
        summary.append(" | Success/Failure: ").append(successCount).append("/").append(failureCount);
        summary.append(" | Rate: ").append(String.format("%.2f%%", successRate)).append("\n");
        
        summary.append("Response time (min/avg/max): ")
              .append(tester.getMinResponseTime()).append("/")
              .append(String.format("%.2f", tester.getAvgResponseTime())).append("/")
              .append(tester.getMaxResponseTime()).append(" ms");
        
        summary.append(" | Status codes: ").append(statusCodeSummary);
        
        // Use info level to ensure it's displayed
        log.info(summary.toString());
    }
    
    /**
     * Clear the display status for a service
     * @param serviceName The name of the service
     */
    public void clearStatus(String serviceName) {
        lastDisplayTime.remove(serviceName);
    }
    
    /**
     * Clear all display statuses
     */
    public void clearAllStatuses() {
        lastDisplayTime.clear();
    }
}
