package com.ironman.controller;

import com.ironman.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    /**
     * Download order invoice as PDF
     */
    @GetMapping("/invoice/{orderId}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long orderId) {

        log.info("Downloading PDF invoice for order: {}", orderId);
        byte[] pdfBytes = reportService.generateOrderInvoicePdf(orderId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice-" + orderId + ".pdf");
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(pdfBytes);
    }

    /**
     * Download order invoice as Excel
     */
    @GetMapping("/invoice/{orderId}/excel")
    public ResponseEntity<byte[]> downloadInvoiceExcel(@PathVariable Long orderId) {

        log.info("Downloading Excel invoice for order: {}", orderId);
        byte[] excelBytes = reportService.generateOrderInvoiceExcel(orderId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "invoice-" + orderId + ".xlsx");
        headers.setContentLength(excelBytes.length);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(excelBytes);
    }

    /**
     * Download orders report as Excel
     */
    @GetMapping("/orders/excel")
    public ResponseEntity<byte[]> downloadOrdersReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Downloading orders report from {} to {}", startDate, endDate);
        byte[] excelBytes = reportService.generateOrdersReportExcel(startDate, endDate);

        String filename = String.format("orders-report-%s-to-%s.xlsx", startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(excelBytes.length);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(excelBytes);
    }

    /**
     * Download revenue report as Excel
     */
    @GetMapping("/revenue/excel")
    public ResponseEntity<byte[]> downloadRevenueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Downloading revenue report from {} to {}", startDate, endDate);
        byte[] excelBytes = reportService.generateRevenueReportExcel(startDate, endDate);

        String filename = String.format("revenue-report-%s-to-%s.xlsx", startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(excelBytes.length);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(excelBytes);
    }
}