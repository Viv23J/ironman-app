package com.ironman.service;

import com.ironman.dto.response.InvoiceData;
import com.ironman.exception.BadRequestException;
import com.ironman.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class ExcelReportService {

    /**
     * Generate orders Excel report
     */
    public byte[] generateOrdersReport(List<Order> orders, LocalDate startDate, LocalDate endDate) {
        log.info("Generating Excel report for {} orders", orders.size());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Orders Report");

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            // Add report header
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("IronMan Laundry - Orders Report");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            // Add date range
            Row dateRangeRow = sheet.createRow(1);
            dateRangeRow.createCell(0).setCellValue(
                    "Period: " + startDate + " to " + endDate);

            // Add empty row
            sheet.createRow(2);

            // Create header row
            Row headerRow = sheet.createRow(3);
            String[] headers = {
                    "Order Number", "Customer Name", "Customer Phone", "Status",
                    "Payment Status", "Pickup Date", "Delivery Date",
                    "Subtotal", "Tax", "Discount", "Total Amount", "Order Date"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Add data rows
            int rowNum = 4;
            BigDecimal totalRevenue = BigDecimal.ZERO;

            for (Order order : orders) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(order.getOrderNumber());
                row.createCell(1).setCellValue(order.getCustomer().getFullName());
                row.createCell(2).setCellValue(order.getCustomer().getPhone());
                row.createCell(3).setCellValue(order.getStatus().name());
                row.createCell(4).setCellValue(order.getPaymentStatus().name());

                Cell pickupCell = row.createCell(5);
                pickupCell.setCellValue(order.getPickupDate().toString());
                pickupCell.setCellStyle(dateStyle);

                Cell deliveryCell = row.createCell(6);
                deliveryCell.setCellValue(order.getExpectedDeliveryDate().toString());
                deliveryCell.setCellStyle(dateStyle);

                Cell subtotalCell = row.createCell(7);
                subtotalCell.setCellValue(order.getSubtotal().doubleValue());
                subtotalCell.setCellStyle(currencyStyle);

                Cell taxCell = row.createCell(8);
                taxCell.setCellValue(order.getTaxAmount().doubleValue());
                taxCell.setCellStyle(currencyStyle);

                Cell discountCell = row.createCell(9);
                discountCell.setCellValue(
                        order.getDiscountAmount() != null ? order.getDiscountAmount().doubleValue() : 0);
                discountCell.setCellStyle(currencyStyle);

                Cell totalCell = row.createCell(10);
                totalCell.setCellValue(order.getTotalAmount().doubleValue());
                totalCell.setCellStyle(currencyStyle);

                Cell orderDateCell = row.createCell(11);
                orderDateCell.setCellValue(order.getCreatedAt().toString());
                orderDateCell.setCellStyle(dateStyle);

                // Add to total revenue
                totalRevenue = totalRevenue.add(order.getTotalAmount());
            }

            // Add summary row
            rowNum++;
            Row summaryRow = sheet.createRow(rowNum);
            Cell summaryLabel = summaryRow.createCell(9);
            summaryLabel.setCellValue("Total Revenue:");
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            summaryLabel.setCellStyle(boldStyle);

            Cell summaryValue = summaryRow.createCell(10);
            summaryValue.setCellValue(totalRevenue.doubleValue());
            CellStyle boldCurrencyStyle = createCurrencyStyle(workbook);
            Font boldCurrencyFont = workbook.createFont();
            boldCurrencyFont.setBold(true);
            boldCurrencyStyle.setFont(boldCurrencyFont);
            summaryValue.setCellStyle(boldCurrencyStyle);

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            log.info("Excel report generated successfully");
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error generating Excel report", e);
            throw new BadRequestException("Failed to generate Excel report: " + e.getMessage());
        }
    }

    /**
     * Generate invoice as Excel
     */
    public byte[] generateInvoiceExcel(InvoiceData invoiceData) {
        log.info("Generating Excel invoice for order: {}", invoiceData.getOrderNumber());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Invoice");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            int rowNum = 0;

            // Company header
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("IronMan Laundry Service");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 18);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            rowNum++; // Empty row

            // Invoice details
            createLabelValueRow(sheet, rowNum++, "Invoice Number:", invoiceData.getOrderNumber(), workbook);
            createLabelValueRow(sheet, rowNum++, "Invoice Date:",
                    invoiceData.getOrderDate().toString(), workbook);
            createLabelValueRow(sheet, rowNum++, "Payment Status:",
                    invoiceData.getPaymentStatus(), workbook);

            rowNum++; // Empty row

            // Customer details
            Row customerTitle = sheet.createRow(rowNum++);
            customerTitle.createCell(0).setCellValue("Customer Details");
            customerTitle.getCell(0).setCellStyle(headerStyle);

            createLabelValueRow(sheet, rowNum++, "Name:", invoiceData.getCustomerName(), workbook);
            createLabelValueRow(sheet, rowNum++, "Phone:", invoiceData.getCustomerPhone(), workbook);
            if (invoiceData.getCustomerEmail() != null) {
                createLabelValueRow(sheet, rowNum++, "Email:", invoiceData.getCustomerEmail(), workbook);
            }

            rowNum++; // Empty row

            // Items header
            Row itemsHeader = sheet.createRow(rowNum++);
            String[] itemHeaders = {"Service", "Cloth Type", "Quantity", "Unit Price", "Total"};
            for (int i = 0; i < itemHeaders.length; i++) {
                Cell cell = itemsHeader.createCell(i);
                cell.setCellValue(itemHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            // Items
            for (InvoiceData.InvoiceItem item : invoiceData.getItems()) {
                Row itemRow = sheet.createRow(rowNum++);
                itemRow.createCell(0).setCellValue(item.getServiceName());
                itemRow.createCell(1).setCellValue(item.getClothType());
                itemRow.createCell(2).setCellValue(item.getQuantity());

                Cell unitPriceCell = itemRow.createCell(3);
                unitPriceCell.setCellValue(item.getUnitPrice().doubleValue());
                unitPriceCell.setCellStyle(currencyStyle);

                Cell totalPriceCell = itemRow.createCell(4);
                totalPriceCell.setCellValue(item.getTotalPrice().doubleValue());
                totalPriceCell.setCellStyle(currencyStyle);
            }

            rowNum++; // Empty row

            // Pricing summary
            createLabelValueRow(sheet, rowNum++, "Subtotal:",
                    "₹" + invoiceData.getSubtotal(), workbook);
            createLabelValueRow(sheet, rowNum++, "Add-on Charges:",
                    "₹" + invoiceData.getAddonCharges(), workbook);
            createLabelValueRow(sheet, rowNum++, "Tax (18% GST):",
                    "₹" + invoiceData.getTaxAmount(), workbook);

            if (invoiceData.getDiscountAmount() != null &&
                    invoiceData.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                createLabelValueRow(sheet, rowNum++,
                        "Discount (" + invoiceData.getCouponCode() + "):",
                        "-₹" + invoiceData.getDiscountAmount(), workbook);
            }

            // Total
            Row totalRow = sheet.createRow(rowNum);
            Cell totalLabel = totalRow.createCell(0);
            totalLabel.setCellValue("TOTAL AMOUNT:");
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldFont.setFontHeightInPoints((short) 12);
            boldStyle.setFont(boldFont);
            totalLabel.setCellStyle(boldStyle);

            Cell totalValue = totalRow.createCell(1);
            totalValue.setCellValue("₹" + invoiceData.getTotalAmount());
            totalValue.setCellStyle(boldStyle);

            // Auto-size columns
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            log.info("Excel invoice generated successfully");
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error generating Excel invoice", e);
            throw new BadRequestException("Failed to generate Excel invoice: " + e.getMessage());
        }
    }

    // =============================================
    // PRIVATE HELPER METHODS
    // =============================================

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("dd-mmm-yyyy"));
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("₹#,##0.00"));
        return style;
    }

    private void createLabelValueRow(Sheet sheet, int rowNum, String label, String value, Workbook workbook) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);

        CellStyle boldStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldStyle.setFont(boldFont);
        labelCell.setCellStyle(boldStyle);

        row.createCell(1).setCellValue(value);
    }
}