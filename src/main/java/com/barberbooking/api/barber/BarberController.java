package com.barberbooking.api.barber;

import java.util.List;

import com.barberbooking.api.common.enums.RecordStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/barbers")
public class BarberController {
    private final BarberRepository barberRepository;

    public BarberController(BarberRepository barberRepository) {
        this.barberRepository = barberRepository;
    }

    @GetMapping
    public List<BarberResponse> list(@RequestParam(defaultValue = "1") Long shopId) {
        return barberRepository.findByShopIdAndStatusOrderBySortOrderAsc(shopId, RecordStatus.ACTIVE)
                .stream()
                .map(BarberResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public BarberResponse detail(@PathVariable Long id) {
        return barberRepository.findById(id)
                .map(BarberResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Barber not found"));
    }

    public record BarberResponse(
            Long id,
            Long shopId,
            String name,
            String avatarUrl,
            String title,
            String description,
            String specialties,
            String status,
            Integer sortOrder
    ) {
        static BarberResponse from(Barber barber) {
            return new BarberResponse(
                    barber.getId(),
                    barber.getShop().getId(),
                    barber.getName(),
                    barber.getAvatarUrl(),
                    barber.getTitle(),
                    barber.getDescription(),
                    barber.getSpecialties(),
                    barber.getStatus().name(),
                    barber.getSortOrder()
            );
        }
    }
}
