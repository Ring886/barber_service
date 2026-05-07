package com.barberbooking.api.shop;

import java.util.List;

import com.barberbooking.api.common.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    List<Shop> findByStatus(RecordStatus status);
}
