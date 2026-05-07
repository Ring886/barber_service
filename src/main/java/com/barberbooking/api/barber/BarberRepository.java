package com.barberbooking.api.barber;

import java.util.List;

import com.barberbooking.api.common.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarberRepository extends JpaRepository<Barber, Long> {
    List<Barber> findByShopIdAndStatusOrderBySortOrderAsc(Long shopId, RecordStatus status);
}
