package com.barberbooking.api.service;

import java.util.List;

import com.barberbooking.api.common.enums.RecordStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/services")
public class ServiceItemController {
    private final ServiceItemRepository serviceItemRepository;

    public ServiceItemController(ServiceItemRepository serviceItemRepository) {
        this.serviceItemRepository = serviceItemRepository;
    }

    @GetMapping
    public List<ServiceItemResponse> list(@RequestParam(defaultValue = "1") Long shopId) {
        return serviceItemRepository.findByShopIdAndStatusOrderBySortOrderAsc(shopId, RecordStatus.ACTIVE)
                .stream()
                .map(ServiceItemResponse::from)
                .toList();
    }

    public record ServiceItemResponse(
            Long id,
            Long shopId,
            String name,
            String description,
            Integer priceCents,
            Integer price,
            Integer durationMinutes,
            String imageUrl,
            String status,
            Integer sortOrder
    ) {
        static ServiceItemResponse from(ServiceItem item) {
            return new ServiceItemResponse(
                    item.getId(),
                    item.getShop().getId(),
                    item.getName(),
                    item.getDescription(),
                    item.getPriceCents(),
                    item.getPriceCents() == null ? null : item.getPriceCents() / 100,
                    item.getDurationMinutes(),
                    item.getImageUrl(),
                    item.getStatus().name(),
                    item.getSortOrder()
            );
        }
    }
}
