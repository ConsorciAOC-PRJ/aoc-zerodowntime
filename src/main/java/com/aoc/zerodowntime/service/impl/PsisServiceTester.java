package com.aoc.zerodowntime.service.impl;

import com.aoc.zerodowntime.model.ServiceType;
import com.aoc.zerodowntime.model.TestResult;
import com.aoc.zerodowntime.service.AbstractServiceTester;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

/**
 * Example implementation of a specific service tester for PSIS
 * This is just an example and can be extended with more specific functionality
 */
@Slf4j
public class PsisServiceTester extends AbstractServiceTester {

    private final String url;
    private final RestTemplate restTemplate;
    
    public PsisServiceTester(String url) {
        this.url = url;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getServiceName() {
        return "PSIS";
    }

    @Override
    protected ServiceType getServiceType() {
        return ServiceType.REST;
    }

    @Override
    public TestResult executeTest() {
        LocalDateTime timestamp = LocalDateTime.now();
        long startTime = System.currentTimeMillis();
        
        try {
            // Here you can add specific headers, authentication, or request body for PSIS
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            long responseTime = System.currentTimeMillis() - startTime;
            
            // You can add specific validation for PSIS response here
            boolean validResponse = true;
            if (response.getBody() != null) {
                // Example: Check if response contains expected data
                // validResponse = response.getBody().contains("expectedData");
            }
            
            boolean success = response.getStatusCode().is2xxSuccessful() && validResponse;
            
            return TestResult.builder()
                    .serviceName(getServiceName())
                    .serviceType(getServiceType())
                    .timestamp(timestamp)
                    .statusCode(response.getStatusCode().value())
                    .success(success)
                    .responseTimeMs(responseTime)
                    .errorMessage(!validResponse ? "Invalid response content" : null)
                    .build();
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Error testing PSIS service: {}", e.getMessage());
            
            return TestResult.builder()
                    .serviceName(getServiceName())
                    .serviceType(getServiceType())
                    .timestamp(timestamp)
                    .statusCode(-1)
                    .success(false)
                    .responseTimeMs(responseTime)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}
