package com.ironman.service;

import com.ironman.dto.response.InvoiceData;
import com.ironman.exception.BadRequestException;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class PdfReportService {

    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(79, 129, 189);
    private static final DeviceRgb LIGHT_GRAY = new DeviceRgb(242, 242, 242);

    /**
     * Generate invoice PDF
     */
    public byte[] generateInvoice(InvoiceData invoiceData) {
        log.info("Generating invoice PDF for order: {}", invoiceData.getOrderNumber());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add header
            addInvoiceHeader(document, invoiceData);

            // Add customer details
            addCustomerDetails(document, invoiceData);

            // Add order details
            addOrderDetails(document, invoiceData);

            // Add items table
            addItemsTable(document, invoiceData);

            // Add pricing summary
            addPricingSummary(document, invoiceData);

            // Add footer
            addInvoiceFooter(document);

            document.close();

            log.info("Invoice PDF generated successfully");
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generating invoice PDF", e);
            throw new BadRequestException("Failed to generate invoice: " + e.getMessage());
        }
    }

    /**
     * Add invoice header
     */
    private void addInvoiceHeader(Document document, InvoiceData data) {
        // Company name
        Paragraph companyName = new Paragraph("IronMan Laundry Service")
                .setFontSize(24)
                .setBold()
                .setFontColor(HEADER_COLOR);
        document.add(companyName);

        // Invoice title
        Paragraph invoiceTitle = new Paragraph("INVOICE")
                .setFontSize(18)
                .setBold()
                .setMarginTop(10);
        document.add(invoiceTitle);

        // Invoice details
        Table headerTable = new Table(2);
        headerTable.setWidth(UnitValue.createPercentValue(100));

        headerTable.addCell(createCell("Invoice Number:", false));
        headerTable.addCell(createCell(data.getOrderNumber(), false));

        headerTable.addCell(createCell("Invoice Date:", false));
        headerTable.addCell(createCell(
                data.getOrderDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), false));

        headerTable.addCell(createCell("Payment Status:", false));
        headerTable.addCell(createCell(data.getPaymentStatus(), false));

        document.add(headerTable);
        document.add(new Paragraph("\n"));
    }

    /**
     * Add customer details
     */
    private void addCustomerDetails(Document document, InvoiceData data) {
        Paragraph customerTitle = new Paragraph("Customer Details")
                .setFontSize(14)
                .setBold()
                .setMarginTop(10);
        document.add(customerTitle);

        Table customerTable = new Table(2);
        customerTable.setWidth(UnitValue.createPercentValue(100));

        customerTable.addCell(createCell("Name:", false));
        customerTable.addCell(createCell(data.getCustomerName(), false));

        customerTable.addCell(createCell("Phone:", false));
        customerTable.addCell(createCell(data.getCustomerPhone(), false));

        if (data.getCustomerEmail() != null) {
            customerTable.addCell(createCell("Email:", false));
            customerTable.addCell(createCell(data.getCustomerEmail(), false));
        }

        customerTable.addCell(createCell("Billing Address:", false));
        customerTable.addCell(createCell(data.getBillingAddress(), false));

        document.add(customerTable);
        document.add(new Paragraph("\n"));
    }

    /**
     * Add order details
     */
    private void addOrderDetails(Document document, InvoiceData data) {
        Paragraph orderTitle = new Paragraph("Order Details")
                .setFontSize(14)
                .setBold()
                .setMarginTop(10);
        document.add(orderTitle);

        Table orderTable = new Table(2);
        orderTable.setWidth(UnitValue.createPercentValue(100));

        orderTable.addCell(createCell("Pickup Address:", false));
        orderTable.addCell(createCell(data.getPickupAddress(), false));

        orderTable.addCell(createCell("Delivery Address:", false));
        orderTable.addCell(createCell(data.getDeliveryAddress(), false));

        orderTable.addCell(createCell("Pickup Date:", false));
        orderTable.addCell(createCell(data.getPickupDate().toString(), false));

        orderTable.addCell(createCell("Expected Delivery:", false));
        orderTable.addCell(createCell(data.getExpectedDeliveryDate().toString(), false));

        document.add(orderTable);
        document.add(new Paragraph("\n"));
    }

    /**
     * Add items table
     */
    private void addItemsTable(Document document, InvoiceData data) {
        Paragraph itemsTitle = new Paragraph("Order Items")
                .setFontSize(14)
                .setBold()
                .setMarginTop(10);
        document.add(itemsTitle);

        Table itemsTable = new Table(new float[]{3, 2, 1, 2, 2});
        itemsTable.setWidth(UnitValue.createPercentValue(100));

        // Header
        itemsTable.addHeaderCell(createHeaderCell("Service"));
        itemsTable.addHeaderCell(createHeaderCell("Cloth Type"));
        itemsTable.addHeaderCell(createHeaderCell("Qty"));
        itemsTable.addHeaderCell(createHeaderCell("Unit Price"));
        itemsTable.addHeaderCell(createHeaderCell("Total"));

        // Items
        for (InvoiceData.InvoiceItem item : data.getItems()) {
            itemsTable.addCell(createCell(item.getServiceName(), false));
            itemsTable.addCell(createCell(item.getClothType(), false));
            itemsTable.addCell(createCell(item.getQuantity().toString(), false));
            itemsTable.addCell(createCell("₹" + item.getUnitPrice(), false));
            itemsTable.addCell(createCell("₹" + item.getTotalPrice(), false));
        }

        document.add(itemsTable);
        document.add(new Paragraph("\n"));
    }

    /**
     * Add pricing summary
     */
    private void addPricingSummary(Document document, InvoiceData data) {
        Table pricingTable = new Table(new float[]{3, 1});
        pricingTable.setWidth(UnitValue.createPercentValue(50));
        pricingTable.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);

        pricingTable.addCell(createCell("Subtotal:", false));
        pricingTable.addCell(createCell("₹" + data.getSubtotal(), false));

        if (data.getAddonCharges().compareTo(java.math.BigDecimal.ZERO) > 0) {
            pricingTable.addCell(createCell("Add-on Charges:", false));
            pricingTable.addCell(createCell("₹" + data.getAddonCharges(), false));
        }

        pricingTable.addCell(createCell("Tax (18% GST):", false));
        pricingTable.addCell(createCell("₹" + data.getTaxAmount(), false));

        if (data.getDiscountAmount() != null &&
                data.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
            pricingTable.addCell(createCell("Discount (" + data.getCouponCode() + "):", false));
            pricingTable.addCell(createCell("-₹" + data.getDiscountAmount(), false));
        }

        // Total with different style
        Cell totalLabelCell = createCell("Total Amount:", true);
        totalLabelCell.setBackgroundColor(LIGHT_GRAY);
        pricingTable.addCell(totalLabelCell);

        Cell totalValueCell = createCell("₹" + data.getTotalAmount(), true);
        totalValueCell.setBackgroundColor(LIGHT_GRAY);
        pricingTable.addCell(totalValueCell);

        document.add(pricingTable);
    }

    /**
     * Add invoice footer
     */
    private void addInvoiceFooter(Document document) {
        document.add(new Paragraph("\n\n"));

        Paragraph footer = new Paragraph("Thank you for choosing IronMan Laundry Service!")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setItalic();
        document.add(footer);

        Paragraph contact = new Paragraph("Contact: support@ironman.com | Phone: +91 1800-XXX-XXXX")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(8);
        document.add(contact);
    }

    /**
     * Create table cell
     */
    private Cell createCell(String text, boolean bold) {
        Paragraph p = new Paragraph(text);
        if (bold) {
            p.setBold();
        }
        Cell cell = new Cell().add(p);
        cell.setBorder(Border.NO_BORDER);
        cell.setPadding(5);
        return cell;
    }

    /**
     * Create header cell
     */
    private Cell createHeaderCell(String text) {
        Cell cell = new Cell().add(new Paragraph(text).setBold().setFontColor(ColorConstants.WHITE));
        cell.setBackgroundColor(HEADER_COLOR);
        cell.setPadding(8);
        cell.setTextAlignment(TextAlignment.CENTER);
        return cell;
    }
}