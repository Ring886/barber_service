package com.barberbooking.api.shop;

import java.time.format.DateTimeFormatter;

import com.barberbooking.api.common.enums.RecordStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/shops")
public class ShopController {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private final ShopRepository shopRepository;

    public ShopController(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    @GetMapping("/current")
    public ResponseEntity<ShopResponse> current() {
        Shop shop = shopRepository.findByStatus(RecordStatus.ACTIVE).stream()
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active shop"));
        return ResponseEntity.ok(ShopResponse.from(shop));
    }

    public record ShopResponse(
            Long id,
            String name,
            String address,
            String phone,
            String description,
            String openingTime,
            String closingTime,
            String status
    ) {
        static ShopResponse from(Shop shop) {
            return new ShopResponse(
                    shop.getId(),
                    shop.getName(),
                    shop.getAddress(),
                    shop.getPhone(),
                    shop.getDescription(),
                    shop.getOpeningTime() == null ? null : shop.getOpeningTime().format(TIME_FORMAT),
                    shop.getClosingTime() == null ? null : shop.getClosingTime().format(TIME_FORMAT),
                    shop.getStatus().name()
            );
        }
    }
}
