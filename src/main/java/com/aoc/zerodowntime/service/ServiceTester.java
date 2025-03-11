package com.aoc.zerodowntime.service;

import com.aoc.zerodowntime.model.TestResult;
import com.aoc.zerodowntime.model.TestSummary;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface ServiceTester {
    
    /**
     * Get the name of the service being tested
     * @return service name
     */
    String getServiceName();
    
    /**
     * Execute a single test against the service
     * @return test result
     */
    TestResult executeTest();
    
    /**
     * Start continuous testing of the service
     * @param intervalMs interval between tests in milliseconds
     * @return CompletableFuture that will be completed when testing is stopped
     */
    CompletableFuture<TestSummary> startContinuousTest(long intervalMs);
    
    /**
     * Stop the continuous testing
     */
    void stopContinuousTest();
    
    /**
     * Check if continuous testing is running
     * @return true if testing is running
     */
    boolean isRunning();
    
    /**
     * Get all test results collected so far
     * @return list of test results
     */
    List<TestResult> getResults();
    
    /**
     * Get counts of status codes encountered
     * @return map of status code to count
     */
    Map<Integer, Integer> getStatusCodeCounts();
    
    /**
     * Get count of successful tests
     * @return count of successful tests
     */
    int getSuccessCount();
    
    /**
     * Get count of failed tests
     * @return count of failed tests
     */
    int getFailureCount();
    
    /**
     * Get minimum response time in milliseconds
     * @return minimum response time
     */
    long getMinResponseTime();
    
    /**
     * Get maximum response time in milliseconds
     * @return maximum response time
     */
    long getMaxResponseTime();
    
    /**
     * Get average response time in milliseconds
     * @return average response time
     */
    double getAvgResponseTime();
    
    /**
     * Get elapsed time since test started
     * @return elapsed time
     */
    Duration getElapsedTime();
    
    /**
     * Generate a summary of the test results
     * @return test summary
     */
    TestSummary generateSummary();
}
