package com.ironman.service;

import com.ironman.model.Order;
import com.ironman.model.OrderStatus;
import com.ironman.model.OrderStatusHistory;
import com.ironman.repository.OrderRepository;
import com.ironman.repository.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStatusService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;

    /**
     * Update order status and log to history
     */
    @Transactional
    public void updateStatus(Order order, OrderStatus newStatus, String changedBy, String notes) {
        OrderStatus previousStatus = order.getStatus();

        // Don't update if status is the same
        if (previousStatus == newStatus) {
            return;
        }

        log.info("Updating order {} status from {} to {}", order.getOrderNumber(), previousStatus, newStatus);

        // Save history
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setNotes(notes);
        statusHistoryRepository.save(history);

        // Update order
        order.setStatus(newStatus);
        orderRepository.save(order);

        log.info("Status updated successfully");
    }

    /**
     * Create initial status history for new order
     */
    @Transactional
    public void createInitialHistory(Order order) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setPreviousStatus(null);
        history.setNewStatus(OrderStatus.PENDING);
        history.setChangedBy("System");
        history.setNotes("Order created");
        statusHistoryRepository.save(history);
    }
}