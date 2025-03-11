package com.aoc.zerodowntime.service.impl;

import com.aoc.zerodowntime.model.ServiceType;
import com.aoc.zerodowntime.model.TestResult;
import com.aoc.zerodowntime.service.AbstractServiceTester;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Slf4j
public class WebsiteServiceTester extends AbstractServiceTester {

    private final String serviceName;
    private final String url;
    private final String expectedContent;
    private final RestTemplate restTemplate;

    public WebsiteServiceTester(String serviceName, String url, String expectedContent) {
        this.serviceName = serviceName;
        this.url = url;
        this.expectedContent = expectedContent;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    protected ServiceType getServiceType() {
        return ServiceType.WEBSITE;
    }

    @Override
    public TestResult executeTest() {
        LocalDateTime timestamp = LocalDateTime.now();
        long startTime = System.currentTimeMillis();
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            long responseTime = System.currentTimeMillis() - startTime;
            
            boolean contentMatches = true;
            if (expectedContent != null && !expectedContent.isEmpty()) {
                contentMatches = response.getBody() != null && response.getBody().contains(expectedContent);
            }
            
            boolean success = response.getStatusCode().is2xxSuccessful() && contentMatches;
            
            return TestResult.builder()
                    .serviceName(serviceName)
                    .serviceType(ServiceType.WEBSITE)
                    .timestamp(timestamp)
                    .statusCode(response.getStatusCode().value())
                    .success(success)
                    .responseTimeMs(responseTime)
                    .errorMessage(!contentMatches ? "Expected content not found" : null)
                    .build();
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Error testing website {}: {}", serviceName, e.getMessage());
            
            return TestResult.builder()
                    .serviceName(serviceName)
                    .serviceType(ServiceType.WEBSITE)
                    .timestamp(timestamp)
                    .statusCode(-1)
                    .success(false)
                    .responseTimeMs(responseTime)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}
