package com.ironman.service;

import com.ironman.dto.response.InvoiceData;
import com.ironman.exception.ResourceNotFoundException;
import com.ironman.model.Order;
import com.ironman.model.OrderItem;
import com.ironman.model.PaymentStatus;
import com.ironman.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final OrderRepository orderRepository;
    private final PdfReportService pdfReportService;
    private final ExcelReportService excelReportService;

    /**
     * Generate invoice for order (PDF)
     */
    public byte[] generateOrderInvoicePdf(Long orderId) {
        log.info("Generating PDF invoice for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        InvoiceData invoiceData = mapToInvoiceData(order);
        return pdfReportService.generateInvoice(invoiceData);
    }

    /**
     * Generate invoice for order (Excel)
     */
    public byte[] generateOrderInvoiceExcel(Long orderId) {
        log.info("Generating Excel invoice for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        InvoiceData invoiceData = mapToInvoiceData(order);
        return excelReportService.generateInvoiceExcel(invoiceData);
    }

    /**
     * Generate orders report (Excel)
     */
    public byte[] generateOrdersReportExcel(LocalDate startDate, LocalDate endDate) {
        log.info("Generating orders report from {} to {}", startDate, endDate);

        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> {
                    LocalDate orderDate = o.getCreatedAt().toLocalDate();
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        return excelReportService.generateOrdersReport(orders, startDate, endDate);
    }

    /**
     * Generate revenue report (Excel) - only PAID orders
     */
    public byte[] generateRevenueReportExcel(LocalDate startDate, LocalDate endDate) {
        log.info("Generating revenue report from {} to {}", startDate, endDate);

        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getPaymentStatus() == PaymentStatus.PAID)
                .filter(o -> {
                    LocalDate orderDate = o.getCreatedAt().toLocalDate();
                    return !orderDate.isBefore(startDate) && !orderDate.isAfter(endDate);
                })
                .collect(Collectors.toList());

        return excelReportService.generateOrdersReport(orders, startDate, endDate);
    }

    // =============================================
    // PRIVATE HELPER METHODS
    // =============================================

    /**
     * Map Order to InvoiceData
     */
    private InvoiceData mapToInvoiceData(Order order) {
        // Map items
        List<InvoiceData.InvoiceItem> items = order.getItems().stream()
                .map(this::mapToInvoiceItem)
                .collect(Collectors.toList());

        // Format addresses
        String billingAddress = formatAddress(order.getDeliveryAddress());
        String pickupAddress = formatAddress(order.getPickupAddress());
        String deliveryAddress = formatAddress(order.getDeliveryAddress());

        return InvoiceData.builder()
                .orderNumber(order.getOrderNumber())
                .orderDate(order.getCreatedAt())
                .orderStatus(order.getStatus().name())
                .customerName(order.getCustomer().getFullName())
                .customerPhone(order.getCustomer().getPhone())
                .customerEmail(order.getCustomer().getEmail())
                .billingAddress(billingAddress)
                .pickupAddress(pickupAddress)
                .deliveryAddress(deliveryAddress)
                .pickupDate(order.getPickupDate())
                .expectedDeliveryDate(order.getExpectedDeliveryDate())
                .items(items)
                .subtotal(order.getSubtotal())
                .addonCharges(order.getAddonCharges())
                .taxAmount(order.getTaxAmount())
                .discountAmount(order.getDiscountAmount())
                .couponCode(order.getCouponCode())
                .totalAmount(order.getTotalAmount())
                .paymentStatus(order.getPaymentStatus().name())
                .paymentMethod("Razorpay")
                .paymentDate(order.getCreatedAt())
                .build();
    }

    /**
     * Map OrderItem to InvoiceItem
     */
    private InvoiceData.InvoiceItem mapToInvoiceItem(OrderItem item) {
        return InvoiceData.InvoiceItem.builder()
                .serviceName(item.getService().getName())
                .clothType(item.getClothType().getName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .build();
    }

    /**
     * Format address
     */
    private String formatAddress(com.ironman.model.Address address) {
        StringBuilder sb = new StringBuilder();
        sb.append(address.getAddressLine1());
        if (address.getAddressLine2() != null && !address.getAddressLine2().isEmpty()) {
            sb.append(", ").append(address.getAddressLine2());
        }
        if (address.getLandmark() != null && !address.getLandmark().isEmpty()) {
            sb.append(", Near ").append(address.getLandmark());
        }
        sb.append(", ").append(address.getCity());
        sb.append(", ").append(address.getState());
        sb.append(" - ").append(address.getPincode());
        return sb.toString();
    }
}