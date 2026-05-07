package com.barberbooking.api.service;

import java.util.List;

import com.barberbooking.api.common.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {
    List<ServiceItem> findByShopIdAndStatusOrderBySortOrderAsc(Long shopId, RecordStatus status);
}
