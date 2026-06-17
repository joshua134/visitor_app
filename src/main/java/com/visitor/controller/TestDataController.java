package com.visitor.controller;

import com.visitor.service.TestDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;

@Controller
@RequestMapping("/test-data")
@RequiredArgsConstructor
@Slf4j
public class TestDataController {
    
    private final TestDataService testDataService;
    
    @GetMapping("/generate")
    @ResponseBody
    public String generateTestData(
            @RequestParam(defaultValue = "400") int count,
            @RequestParam(defaultValue = "2024-01-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "2024-12-31") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        TestDataService.TestDataResult result = testDataService.generateTestVisitors(count, startDate, endDate);
        
        return String.format("""
            <html>
            <head><title>Test Data Generation</title></head>
            <body style="font-family: Arial; padding: 20px;">
                <h1>Test Data Generation Complete</h1>
                <hr/>
                <h2>Summary:</h2>
                <ul>
                    <li>Requested: %d</li>
                    <li>Successfully created: %d</li>
                    <li>Failed: %d</li>
                </ul>
                <h2>Date Range:</h2>
                <ul>
                    <li>Start: %s</li>
                    <li>End: %s</li>
                </ul>
                <p><a href="/admin/visitors">View Visitors</a> | <a href="/admin/dashboard">Go to Dashboard</a></p>
            </body>
            </html>
            """, 
            result.getTotalRequested(), 
            result.getSuccessCount(), 
            result.getFailCount(),
            startDate.toString(),
            endDate.toString());
    }
    
    @GetMapping("/generate-pattern")
    @ResponseBody
    public String generateWithPattern(
            @RequestParam(defaultValue = "400") int count,
            @RequestParam(defaultValue = "2024-01-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "2024-12-31") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "RANDOM") TestDataService.DatePattern pattern) {
        
        TestDataService.TestDataResult result = testDataService.generateVisitorsWithPattern(count, pattern, startDate, endDate);
        
        return String.format("""
            <html>
            <head><title>Test Data Generation</title></head>
            <body style="font-family: Arial; padding: 20px;">
                <h1>Test Data Generation Complete</h1>
                <hr/>
                <h2>Summary:</h2>
                <ul>
                    <li>Pattern: %s</li>
                    <li>Requested: %d</li>
                    <li>Successfully created: %d</li>
                    <li>Failed: %d</li>
                </ul>
                <h2>Date Range:</h2>
                <ul>
                    <li>Start: %s</li>
                    <li>End: %s</li>
                </ul>
                <p><a href="/admin/visitors">View Visitors</a> | <a href="/admin/dashboard">Go to Dashboard</a></p>
            </body>
            </html>
            """, 
            pattern.name(),
            result.getTotalRequested(), 
            result.getSuccessCount(), 
            result.getFailCount(),
            startDate.toString(),
            endDate.toString());
    }
    
    @GetMapping("/clear")
    @ResponseBody
    public String clearAllVisitors() {
        // Optional: Add method to clear test data
        return """
            <html>
            <head><title>Clear Test Data</title></head>
            <body style="font-family: Arial; padding: 20px;">
                <h1>Warning: This will delete all visitor data!</h1>
                <p>Use with caution. This action cannot be undone.</p>
                <form action="/test-data/clear-confirm" method="post">
                    <button type="submit" style="background: red; color: white; padding: 10px 20px; border: none; cursor: pointer;">
                        Confirm Delete All Visitors
                    </button>
                </form>
                <p><a href="/admin/dashboard">Cancel</a></p>
            </body>
            </html>
            """;
    }
}