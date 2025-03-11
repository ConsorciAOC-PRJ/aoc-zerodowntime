package com.aoc.zerodowntime.report;

import com.aoc.zerodowntime.model.TestSummary;

import java.nio.file.Path;

public interface ReportGenerator {
    
    /**
     * Generate a report for the given test summary
     * @param summary test summary
     * @return path to the generated report
     */
    Path generateReport(TestSummary summary);
}
