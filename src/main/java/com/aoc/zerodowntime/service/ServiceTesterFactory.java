package com.aoc.zerodowntime.service;

import com.aoc.zerodowntime.model.ServiceType;
import com.aoc.zerodowntime.service.impl.RestServiceTester;
import com.aoc.zerodowntime.service.impl.SoapServiceTester;
import com.aoc.zerodowntime.service.impl.WebsiteServiceTester;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class ServiceTesterFactory {

    public ServiceTester createRestServiceTester(String serviceName, String url, HttpMethod method) {
        return new RestServiceTester(serviceName, url, method);
    }
    
    public ServiceTester createSoapServiceTester(String serviceName, String url, String soapRequest) {
        return new SoapServiceTester(serviceName, url, soapRequest);
    }
    
    public ServiceTester createWebsiteServiceTester(String serviceName, String url, String expectedContent) {
        return new WebsiteServiceTester(serviceName, url, expectedContent);
    }
    
    public ServiceTester createServiceTester(ServiceType type, String serviceName, String url, Object... additionalParams) {
        switch (type) {
            case REST:
                HttpMethod method = HttpMethod.GET;
                if (additionalParams.length > 0 && additionalParams[0] instanceof HttpMethod) {
                    method = (HttpMethod) additionalParams[0];
                }
                return createRestServiceTester(serviceName, url, method);
                
            case SOAP:
                String soapRequest = "";
                if (additionalParams.length > 0 && additionalParams[0] instanceof String) {
                    soapRequest = (String) additionalParams[0];
                }
                return createSoapServiceTester(serviceName, url, soapRequest);
                
            case WEBSITE:
                String expectedContent = "";
                if (additionalParams.length > 0 && additionalParams[0] instanceof String) {
                    expectedContent = (String) additionalParams[0];
                }
                return createWebsiteServiceTester(serviceName, url, expectedContent);
                
            default:
                throw new IllegalArgumentException("Unsupported service type: " + type);
        }
    }
}
