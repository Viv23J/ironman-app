package com.ironman.repository;

import com.ironman.model.SmsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SmsLogRepository extends JpaRepository<SmsLog, Long> {

    List<SmsLog> findByPhoneNumber(String phoneNumber);

    List<SmsLog> findByOrderId(Long orderId);

    List<SmsLog> findBySmsType(String smsType);

    List<SmsLog> findByStatus(String status);
}