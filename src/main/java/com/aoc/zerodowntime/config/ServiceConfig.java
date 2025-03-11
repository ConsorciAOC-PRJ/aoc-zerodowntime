package com.aoc.zerodowntime.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for service URLs across different environments
 */
@Component
public class ServiceConfig {

    @Value("${service.default.environment:pre}")
    private String defaultEnvironment;

    // PRE environment URLs
    @Value("${service.pre.psis.url:https://pre.example.com/psis/api}")
    private String prePsisUrl;
    
    @Value("${service.pre.iarxiu.url:https://pre.example.com/iarxiu/soap}")
    private String preIarxiuUrl;
    
    @Value("${service.pre.signador.url:https://pre.example.com/signador/api}")
    private String preSignadorUrl;
    
    @Value("${service.pre.valid.url:https://pre.example.com/valid/api}")
    private String preValidUrl;
    
    @Value("${service.pre.enotum.url:https://pre.example.com/enotum/api}")
    private String preEnotumUrl;

    // PRO environment URLs
    @Value("${service.pro.psis.url:https://pro.example.com/psis/api}")
    private String proPsisUrl;
    
    @Value("${service.pro.iarxiu.url:https://pro.example.com/iarxiu/soap}")
    private String proIarxiuUrl;
    
    @Value("${service.pro.signador.url:https://pro.example.com/signador/api}")
    private String proSignadorUrl;
    
    @Value("${service.pro.valid.url:https://pro.example.com/valid/api}")
    private String proValidUrl;
    
    @Value("${service.pro.enotum.url:https://pro.example.com/enotum/api}")
    private String proEnotumUrl;

    // Service name to property mapping
    private final Map<String, Map<String, String>> serviceUrls = new HashMap<>();

    /**
     * Initialize the service URL mappings
     */
    public ServiceConfig() {
        init();
    }

    private void init() {
        // Initialize PRE environment URLs
        Map<String, String> preUrls = new HashMap<>();
        preUrls.put("psis", prePsisUrl);
        preUrls.put("iarxiu", preIarxiuUrl);
        preUrls.put("signador", preSignadorUrl);
        preUrls.put("valid", preValidUrl);
        preUrls.put("enotum", preEnotumUrl);
        serviceUrls.put("pre", preUrls);

        // Initialize PRO environment URLs
        Map<String, String> proUrls = new HashMap<>();
        proUrls.put("psis", proPsisUrl);
        proUrls.put("iarxiu", proIarxiuUrl);
        proUrls.put("signador", proSignadorUrl);
        proUrls.put("valid", proValidUrl);
        proUrls.put("enotum", proEnotumUrl);
        serviceUrls.put("pro", proUrls);
    }

    /**
     * Get the URL for a service in the specified environment
     * 
     * @param serviceName the name of the service
     * @param environment the environment (pre or pro)
     * @return the URL for the service
     */
    public String getServiceUrl(String serviceName, String environment) {
        // Normalize environment name to lowercase
        String env = environment.toLowerCase();
        if (!serviceUrls.containsKey(env)) {
            env = defaultEnvironment;
        }
        
        // Normalize service name to lowercase
        String service = serviceName.toLowerCase();
        
        Map<String, String> envUrls = serviceUrls.get(env);
        if (envUrls == null || !envUrls.containsKey(service)) {
            throw new IllegalArgumentException("Unknown service: " + serviceName);
        }
        
        return envUrls.get(service);
    }
    
    /**
     * Get the URL for a service in the default environment
     * 
     * @param serviceName the name of the service
     * @return the URL for the service
     */
    public String getServiceUrl(String serviceName) {
        return getServiceUrl(serviceName, defaultEnvironment);
    }
    
    /**
     * Get the default environment
     * 
     * @return the default environment
     */
    public String getDefaultEnvironment() {
        return defaultEnvironment;
    }
}
