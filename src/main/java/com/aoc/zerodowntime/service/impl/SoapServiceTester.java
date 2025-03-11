package com.aoc.zerodowntime.service.impl;

import com.aoc.zerodowntime.model.ServiceType;
import com.aoc.zerodowntime.model.TestResult;
import com.aoc.zerodowntime.service.AbstractServiceTester;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.xml.transform.StringSource;

import javax.xml.transform.Source;
import java.time.LocalDateTime;

@Slf4j
public class SoapServiceTester extends AbstractServiceTester {

    private final String serviceName;
    private final String url;
    private final String soapRequest;
    private final WebServiceTemplate webServiceTemplate;

    public SoapServiceTester(String serviceName, String url, String soapRequest) {
        this.serviceName = serviceName;
        this.url = url;
        this.soapRequest = soapRequest;
        this.webServiceTemplate = new WebServiceTemplate();
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    protected ServiceType getServiceType() {
        return ServiceType.SOAP;
    }

    @Override
    public TestResult executeTest() {
        LocalDateTime timestamp = LocalDateTime.now();
        long startTime = System.currentTimeMillis();
        
        try {
            Source requestSource = new StringSource(soapRequest);
            webServiceTemplate.sendSourceAndReceive(url, requestSource, response -> {
                // Just consume the response
                return null;
            });
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            return TestResult.builder()
                    .serviceName(serviceName)
                    .serviceType(ServiceType.SOAP)
                    .timestamp(timestamp)
                    .statusCode(200) // Assuming success if no exception
                    .success(true)
                    .responseTimeMs(responseTime)
                    .build();
        } catch (SoapFaultClientException e) {
            // SOAP fault is still a valid response, just with an error
            long responseTime = System.currentTimeMillis() - startTime;
            log.warn("SOAP fault from service {}: {}", serviceName, e.getMessage());
            
            return TestResult.builder()
                    .serviceName(serviceName)
                    .serviceType(ServiceType.SOAP)
                    .timestamp(timestamp)
                    .statusCode(500) // Using 500 for SOAP faults
                    .success(false)
                    .responseTimeMs(responseTime)
                    .errorMessage(e.getMessage())
                    .build();
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Error testing SOAP service {}: {}", serviceName, e.getMessage());
            
            return TestResult.builder()
                    .serviceName(serviceName)
                    .serviceType(ServiceType.SOAP)
                    .timestamp(timestamp)
                    .statusCode(-1)
                    .success(false)
                    .responseTimeMs(responseTime)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }
}
