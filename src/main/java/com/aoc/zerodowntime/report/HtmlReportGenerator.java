package com.aoc.zerodowntime.report;

import com.aoc.zerodowntime.model.TestResult;
import com.aoc.zerodowntime.model.TestSummary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class HtmlReportGenerator implements ReportGenerator {

    private final TemplateEngine templateEngine;
    private final Path reportsDir;
    
    public HtmlReportGenerator() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        
        this.reportsDir = Paths.get("reports");
        try {
            Files.createDirectories(reportsDir);
        } catch (IOException e) {
            log.error("Failed to create reports directory", e);
        }
    }
    
    @Override
    public Path generateReport(TestSummary summary) {
        Context context = new Context();
        context.setVariable("summary", summary);
        context.setVariable("results", summary.getResults());
        context.setVariable("generatedAt", LocalDateTime.now());
        
        String reportHtml = templateEngine.process("report", context);
        
        String fileName = String.format("%s_%s.html", 
                summary.getServiceName().replaceAll("[^a-zA-Z0-9]", "_"),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        
        Path reportPath = reportsDir.resolve(fileName);
        
        try (FileWriter writer = new FileWriter(reportPath.toFile())) {
            writer.write(reportHtml);
            log.info("Report generated at {}", reportPath);
            return reportPath;
        } catch (IOException e) {
            log.error("Failed to write report", e);
            return null;
        }
    }
    
    /**
     * Generate a fallback report when the template is not available
     */
    public Path generateFallbackReport(TestSummary summary) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
            .append("<html>\n")
            .append("<head>\n")
            .append("  <title>Test Report: ").append(summary.getServiceName()).append("</title>\n")
            .append("  <style>\n")
            .append("    body { font-family: Arial, sans-serif; margin: 20px; }\n")
            .append("    h1, h2 { color: #333; }\n")
            .append("    .summary { background-color: #f5f5f5; padding: 15px; border-radius: 5px; margin-bottom: 20px; }\n")
            .append("    .success { color: green; }\n")
            .append("    .failure { color: red; }\n")
            .append("    table { border-collapse: collapse; width: 100%; }\n")
            .append("    th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n")
            .append("    th { background-color: #f2f2f2; }\n")
            .append("    tr:nth-child(even) { background-color: #f9f9f9; }\n")
            .append("  </style>\n")
            .append("</head>\n")
            .append("<body>\n")
            .append("  <h1>Test Report: ").append(summary.getServiceName()).append("</h1>\n")
            .append("  <div class=\"summary\">\n")
            .append("    <h2>Summary</h2>\n")
            .append("    <p>Service Type: ").append(summary.getServiceType()).append("</p>\n")
            .append("    <p>Start Time: ").append(summary.getStartTime()).append("</p>\n")
            .append("    <p>End Time: ").append(summary.getEndTime()).append("</p>\n")
            .append("    <p>Duration: ").append(summary.getTotalDuration().toSeconds()).append(" seconds</p>\n")
            .append("    <p>Total Requests: ").append(summary.getTotalRequests()).append("</p>\n")
            .append("    <p>Successful Requests: ").append(summary.getSuccessfulRequests()).append("</p>\n")
            .append("    <p>Failed Requests: ").append(summary.getFailedRequests()).append("</p>\n")
            .append("    <p>Success Rate: ").append(String.format("%.2f%%", summary.getSuccessRate())).append("</p>\n")
            .append("    <p>Min Response Time: ").append(summary.getMinResponseTime()).append(" ms</p>\n")
            .append("    <p>Max Response Time: ").append(summary.getMaxResponseTime()).append(" ms</p>\n")
            .append("    <p>Avg Response Time: ").append(String.format("%.2f", summary.getAvgResponseTime())).append(" ms</p>\n")
            .append("  </div>\n")
            .append("  <h2>Test Results</h2>\n")
            .append("  <table>\n")
            .append("    <tr>\n")
            .append("      <th>Timestamp</th>\n")
            .append("      <th>Status Code</th>\n")
            .append("      <th>Success</th>\n")
            .append("      <th>Response Time (ms)</th>\n")
            .append("      <th>Error Message</th>\n")
            .append("    </tr>\n");
        
        for (TestResult result : summary.getResults()) {
            html.append("    <tr>\n")
                .append("      <td>").append(result.getTimestamp()).append("</td>\n")
                .append("      <td>").append(result.getStatusCode()).append("</td>\n")
                .append("      <td class=\"").append(result.isSuccess() ? "success" : "failure").append("\">")
                .append(result.isSuccess() ? "Success" : "Failure").append("</td>\n")
                .append("      <td>").append(result.getResponseTimeMs()).append("</td>\n")
                .append("      <td>").append(result.getErrorMessage() != null ? result.getErrorMessage() : "").append("</td>\n")
                .append("    </tr>\n");
        }
        
        html.append("  </table>\n")
            .append("  <p><em>Report generated at: ").append(LocalDateTime.now()).append("</em></p>\n")
            .append("</body>\n")
            .append("</html>");
        
        String fileName = String.format("%s_%s.html", 
                summary.getServiceName().replaceAll("[^a-zA-Z0-9]", "_"),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        
        Path reportPath = reportsDir.resolve(fileName);
        
        try (FileWriter writer = new FileWriter(reportPath.toFile())) {
            writer.write(html.toString());
            log.info("Fallback report generated at {}", reportPath);
            return reportPath;
        } catch (IOException e) {
            log.error("Failed to write fallback report", e);
            return null;
        }
    }
}
