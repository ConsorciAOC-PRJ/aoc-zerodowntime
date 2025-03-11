package com.aoc.zerodowntime.shell;

import com.aoc.zerodowntime.config.ServiceConfig;
import com.aoc.zerodowntime.display.TestStatusDisplay;
import com.aoc.zerodowntime.model.TestSummary;
import com.aoc.zerodowntime.report.ReportGenerator;
import com.aoc.zerodowntime.service.AbstractServiceTester;
import com.aoc.zerodowntime.service.ServiceTester;
import com.aoc.zerodowntime.service.ServiceTesterFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@ShellComponent
@RequiredArgsConstructor
public class ZeroDowntimeCommands {

    private final ServiceTesterFactory serviceTesterFactory;
    private final ReportGenerator reportGenerator;
    private final ServiceConfig serviceConfig;
    private final TestStatusDisplay testStatusDisplay;
    
    private final Map<String, ServiceTester> activeTesters = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<TestSummary>> activeTests = new ConcurrentHashMap<>();
    
    @ShellMethod(value = "List available services", key = "list")
    public String listServices() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available services:\n");
        sb.append("1. PSIS (REST)\n");
        sb.append("2. iArxiu (SOAP)\n");
        sb.append("3. Signador (REST)\n");
        sb.append("4. VALID (REST)\n");
        sb.append("5. eNotum (REST)\n");
        sb.append("6. Custom REST Service\n");
        sb.append("7. Custom SOAP Service\n");
        sb.append("8. Custom Website\n");
        
        if (!activeTesters.isEmpty()) {
            sb.append("\nActive tests:\n");
            activeTesters.forEach((name, tester) -> 
                sb.append("- ").append(name).append(" (").append(tester.getServiceName()).append(")\n"));
        }
        
        return sb.toString();
    }
    
    /**
     * Start a test for PSIS service
     */
    @ShellMethod(value = "Start testing PSIS service", key = "start psis")
    public String startPsisTest(
            @ShellOption(help = "Environment (pre/pro)", defaultValue = "") String env,
            @ShellOption(help = "URL of the service (overrides environment URL)", defaultValue = "") String url,
            @ShellOption(help = "Interval between requests in ms", defaultValue = "1000") long intervalMs) {
        
        String environment = env.isEmpty() ? serviceConfig.getDefaultEnvironment() : env.toLowerCase();
        String serviceUrl = url.isEmpty() ? serviceConfig.getServiceUrl("psis", environment) : url;
        
        return startServiceTest("PSIS", serviceUrl, HttpMethod.GET, intervalMs, environment);
    }
    
    /**
     * Start a test for iArxiu service
     */
    @ShellMethod(value = "Start testing iArxiu service", key = "start iarxiu")
    public String startIarxiuTest(
            @ShellOption(help = "Environment (pre/pro)", defaultValue = "") String env,
            @ShellOption(help = "URL of the service (overrides environment URL)", defaultValue = "") String url,
            @ShellOption(help = "Interval between requests in ms", defaultValue = "1000") long intervalMs) {
        
        String environment = env.isEmpty() ? serviceConfig.getDefaultEnvironment() : env.toLowerCase();
        String serviceUrl = url.isEmpty() ? serviceConfig.getServiceUrl("iarxiu", environment) : url;
        
        String soapRequest = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soapenv:Header/><soapenv:Body><ping/></soapenv:Body></soapenv:Envelope>";
        
        return startSoapServiceTest("iArxiu", serviceUrl, soapRequest, intervalMs, environment);
    }
    
    /**
     * Start a test for Signador service
     */
    @ShellMethod(value = "Start testing Signador service", key = "start signador")
    public String startSignadorTest(
            @ShellOption(help = "Environment (pre/pro)", defaultValue = "") String env,
            @ShellOption(help = "URL of the service (overrides environment URL)", defaultValue = "") String url,
            @ShellOption(help = "Interval between requests in ms", defaultValue = "1000") long intervalMs) {
        
        String environment = env.isEmpty() ? serviceConfig.getDefaultEnvironment() : env.toLowerCase();
        String serviceUrl = url.isEmpty() ? serviceConfig.getServiceUrl("signador", environment) : url;
        
        return startServiceTest("Signador", serviceUrl, HttpMethod.GET, intervalMs, environment);
    }
    
    /**
     * Start a test for VALID service
     */
    @ShellMethod(value = "Start testing VALID service", key = "start valid")
    public String startValidTest(
            @ShellOption(help = "Environment (pre/pro)", defaultValue = "") String env,
            @ShellOption(help = "URL of the service (overrides environment URL)", defaultValue = "") String url,
            @ShellOption(help = "Interval between requests in ms", defaultValue = "1000") long intervalMs) {
        
        String environment = env.isEmpty() ? serviceConfig.getDefaultEnvironment() : env.toLowerCase();
        String serviceUrl = url.isEmpty() ? serviceConfig.getServiceUrl("valid", environment) : url;
        
        return startServiceTest("VALID", serviceUrl, HttpMethod.GET, intervalMs, environment);
    }
    
    /**
     * Start a test for eNotum service
     */
    @ShellMethod(value = "Start testing eNotum service", key = "start enotum")
    public String startEnotumTest(
            @ShellOption(help = "Environment (pre/pro)", defaultValue = "") String env,
            @ShellOption(help = "URL of the service (overrides environment URL)", defaultValue = "") String url,
            @ShellOption(help = "Interval between requests in ms", defaultValue = "1000") long intervalMs) {
        
        String environment = env.isEmpty() ? serviceConfig.getDefaultEnvironment() : env.toLowerCase();
        String serviceUrl = url.isEmpty() ? serviceConfig.getServiceUrl("enotum", environment) : url;
        
        return startServiceTest("eNotum", serviceUrl, HttpMethod.GET, intervalMs, environment);
    }
    
    /**
     * Helper method to start a REST service test
     */
    private String startServiceTest(String serviceName, String url, HttpMethod method, long intervalMs, String environment) {
        if (activeTesters.containsKey(serviceName)) {
            return "Test for " + serviceName + " is already running. Stop it first.";
        }
        
        ServiceTester tester = serviceTesterFactory.createRestServiceTester(serviceName, url, method);
        
        // Set the status display for the tester
        if (tester instanceof AbstractServiceTester) {
            ((AbstractServiceTester) tester).setStatusDisplay(testStatusDisplay);
        }
        
        activeTesters.put(serviceName, tester);
        CompletableFuture<TestSummary> future = tester.startContinuousTest(intervalMs);
        activeTests.put(serviceName, future);
        
        return "Started testing " + serviceName + " (" + environment + ") at " + url + " with interval " + intervalMs + "ms\n" +
               "Test is running in the background. Use 'status' command to view detailed statistics.";
    }
    
    /**
     * Helper method to start a SOAP service test
     */
    private String startSoapServiceTest(String serviceName, String url, String soapRequest, long intervalMs, String environment) {
        if (activeTesters.containsKey(serviceName)) {
            return "Test for " + serviceName + " is already running. Stop it first.";
        }
        
        ServiceTester tester = serviceTesterFactory.createSoapServiceTester(serviceName, url, soapRequest);
        
        // Set the status display for the tester
        if (tester instanceof AbstractServiceTester) {
            ((AbstractServiceTester) tester).setStatusDisplay(testStatusDisplay);
        }
        
        activeTesters.put(serviceName, tester);
        CompletableFuture<TestSummary> future = tester.startContinuousTest(intervalMs);
        activeTests.put(serviceName, future);
        
        return "Started testing " + serviceName + " (" + environment + ") at " + url + " with interval " + intervalMs + "ms\n" +
               "Test is running in the background. Use 'status' command to view detailed statistics.";
    }
    
    /**
     * Legacy method for backward compatibility
     */
    @ShellMethod(value = "Start testing a service (legacy)", key = "start", 
            prefix = "Start testing a service with the specified service number.\n" +
                  "Usage: start <service-number> [--env <environment>] [--url <url>] [--interval-ms <interval>]\n" +
                  "Example: start 1 --env pre --interval-ms 500")
    public String startTest(
            @ShellOption(help = "Service number from the list") int serviceNumber,
            @ShellOption(help = "Environment (pre/pro)", defaultValue = "") String env,
            @ShellOption(help = "URL of the service (overrides environment URL)", defaultValue = "") String url,
            @ShellOption(help = "Interval between requests in ms", defaultValue = "1000") long intervalMs) {
        
        String environment = env.isEmpty() ? serviceConfig.getDefaultEnvironment() : env.toLowerCase();
        
        switch (serviceNumber) {
            case 1: // PSIS
                return startPsisTest(environment, url, intervalMs);
                
            case 2: // iArxiu
                return startIarxiuTest(environment, url, intervalMs);
                
            case 3: // Signador
                return startSignadorTest(environment, url, intervalMs);
                
            case 4: // VALID
                return startValidTest(environment, url, intervalMs);
                
            case 5: // eNotum
                return startEnotumTest(environment, url, intervalMs);
                
            case 6: // Custom REST
                if (url.isEmpty()) {
                    return "URL is required for custom REST service";
                }
                return startServiceTest("CustomREST", url, HttpMethod.GET, intervalMs, environment);
                
            case 7: // Custom SOAP
                if (url.isEmpty()) {
                    return "URL is required for custom SOAP service";
                }
                String customSoapRequest = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                        "<soapenv:Header/><soapenv:Body><ping/></soapenv:Body></soapenv:Envelope>";
                return startSoapServiceTest("CustomSOAP", url, customSoapRequest, intervalMs, environment);
                
            case 8: // Custom Website
                if (url.isEmpty()) {
                    return "URL is required for custom website";
                }
                ServiceTester tester = serviceTesterFactory.createWebsiteServiceTester("CustomWebsite", url, "");
                
                // Set the status display for the tester
                if (tester instanceof AbstractServiceTester) {
                    ((AbstractServiceTester) tester).setStatusDisplay(testStatusDisplay);
                }
                
                activeTesters.put("CustomWebsite", tester);
                CompletableFuture<TestSummary> future = tester.startContinuousTest(intervalMs);
                activeTests.put("CustomWebsite", future);
                
                return "Started testing CustomWebsite (" + environment + ") at " + url + " with interval " + intervalMs + "ms\n" +
                       "Test is running in the background. Use 'status' command to view detailed statistics.";
                
            default:
                return "Invalid service number. Use 'list' command to see available services.";
        }
    }
    
    @ShellMethod(value = "Stop testing a service", key = "stop",
            prefix = "Stop testing a specific service and generate a report.\n" +
                  "Usage: stop <service-name>\n" +
                  "Example: stop PSIS")
    @ShellMethodAvailability("isTestRunning")
    public String stopTest(@ShellOption(help = "Service name") String serviceName) {
        ServiceTester tester = activeTesters.get(serviceName);
        if (tester == null) {
            return "No test running for " + serviceName;
        }
        
        tester.stopContinuousTest();
        
        CompletableFuture<TestSummary> future = activeTests.remove(serviceName);
        activeTesters.remove(serviceName);
        
        TestSummary summary = future.join();
        Path reportPath = reportGenerator.generateReport(summary);
        
        return "Stopped testing " + serviceName + "\n" +
                "Total requests: " + summary.getTotalRequests() + "\n" +
                "Success rate: " + String.format("%.2f%%", summary.getSuccessRate()) + "\n" +
                "Report generated at: " + reportPath;
    }
    
    @ShellMethod(value = "Stop all running tests", key = "stop-all",
            prefix =  "Stop all running tests and generate reports for each.")
    @ShellMethodAvailability("isAnyTestRunning")
    public String stopAllTests() {
        StringBuilder sb = new StringBuilder("Stopping all tests:\n");
        
        for (String serviceName : new HashMap<>(activeTesters).keySet()) {
            sb.append(stopTest(serviceName)).append("\n");
        }
        
        return sb.toString();
    }
    
    @ShellMethod(value = "Show status of running tests", key = "status",
            prefix = "Show detailed status of all running tests, including:\n" +
                  "- Elapsed time\n" +
                  "- Total requests sent\n" +
                  "- Success/failure counts\n" +
                  "- Response time statistics\n" +
                  "- Status code distribution")
    public String showStatus() {
        if (activeTesters.isEmpty()) {
            return "No tests are currently running";
        }
        
        StringBuilder sb = new StringBuilder("Running tests:\n");
        
        activeTesters.forEach((name, tester) -> {
            Duration elapsed = tester.getElapsedTime();
            String elapsedTime = String.format("%02d:%02d:%02d", 
                    elapsed.toHours(), 
                    elapsed.toMinutesPart(), 
                    elapsed.toSecondsPart());
            
            sb.append("\n=== ").append(name).append(" (").append(tester.getServiceName()).append(") ===\n");
            sb.append("Elapsed time: ").append(elapsedTime).append("\n");
            sb.append("Total requests: ").append(tester.getResults().size()).append("\n");
            sb.append("Success/Failure: ").append(tester.getSuccessCount()).append("/").append(tester.getFailureCount()).append("\n");
            sb.append("Success rate: ").append(String.format("%.2f%%", 
                    tester.getResults().size() > 0 ? 
                    (double) tester.getSuccessCount() / tester.getResults().size() * 100 : 0)).append("\n");
            
            sb.append("Response time (min/avg/max): ")
              .append(tester.getMinResponseTime()).append("/")
              .append(String.format("%.2f", tester.getAvgResponseTime())).append("/")
              .append(tester.getMaxResponseTime()).append(" ms\n");
            
            Map<Integer, Integer> statusCodes = tester.getStatusCodeCounts();
            if (!statusCodes.isEmpty()) {
                sb.append("Status codes: ");
                sb.append(statusCodes.entrySet().stream()
                        .map(e -> e.getKey() + " (" + e.getValue() + ")")
                        .collect(Collectors.joining(", ")));
                sb.append("\n");
            }
        });
        
        return sb.toString();
    }
    
    public Availability isTestRunning() {
        return !activeTesters.isEmpty()
                ? Availability.available()
                : Availability.unavailable("no tests are currently running");
    }
    
    public Availability isAnyTestRunning() {
        return !activeTesters.isEmpty()
                ? Availability.available()
                : Availability.unavailable("no tests are currently running");
    }
}
