package com.aoc.zerodowntime.service.impl;

import com.aoc.zerodowntime.model.ServiceType;
import com.aoc.zerodowntime.model.TestResult;
import com.aoc.zerodowntime.service.AbstractServiceTester;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Slf4j
public class RestServiceTester extends AbstractServiceTester {

    private final String serviceName;
    private final String url;
    private final HttpMethod method;
    private final RestTemplate restTemplate;
    
    public RestServiceTester(String serviceName, String url, HttpMethod method) {
        this.serviceName = serviceName;
        this.url = url;
        this.method = method;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public String getServiceName() {
        return serviceName;
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
            ResponseEntity<String> response = restTemplate.exchange(url, method, null, String.class);
            long responseTime = System.currentTimeMillis() - startTime;
            
            return TestResult.builder()
                    .serviceName(serviceName)
                    .serviceType(ServiceType.REST)
                    .timestamp(timestamp)
                    .statusCode(response.getStatusCode().value())
                    .success(response.getStatusCode().is2xxSuccessful())
                    .responseTimeMs(responseTime)
                    .build();
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Error testing REST service {}: {}", serviceName, e.getMessage());
            
            return TestResult.builder()
                    .serviceName(serviceName)
                    .serviceType(ServiceType.REST)
                    .timestamp(timestamp)
                    .statusCode(-1)
                    .success(false)
                    .responseTimeMs(responseTime)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}
