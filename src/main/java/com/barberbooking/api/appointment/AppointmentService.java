package com.barberbooking.api.appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import com.barberbooking.api.barber.Barber;
import com.barberbooking.api.barber.BarberRepository;
import com.barberbooking.api.service.ServiceItem;
import com.barberbooking.api.service.ServiceItemRepository;
import com.barberbooking.api.shop.Shop;
import com.barberbooking.api.shop.ShopRepository;
import com.barberbooking.api.user.User;
import com.barberbooking.api.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AppointmentService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final List<AppointmentStatus> ACTIVE_STATUSES = List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final BarberRepository barberRepository;
    private final ServiceItemRepository serviceItemRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            UserRepository userRepository,
            ShopRepository shopRepository,
            BarberRepository barberRepository,
            ServiceItemRepository serviceItemRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.barberRepository = barberRepository;
        this.serviceItemRepository = serviceItemRepository;
    }

    @Transactional(readOnly = true)
    public List<AvailableSlotResponse> availableSlots(Long barberId, Long serviceId, LocalDate date) {
        Barber barber = barberRepository.findById(barberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Barber not found"));
        ServiceItem service = serviceItemRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
        Shop shop = barber.getShop();

        LocalTime open = shop.getOpeningTime() == null ? LocalTime.of(10, 0) : shop.getOpeningTime();
        LocalTime close = shop.getClosingTime() == null ? LocalTime.of(21, 0) : shop.getClosingTime();
        int duration = service.getDurationMinutes() == null ? 45 : service.getDurationMinutes();
        int step = duration <= 60 ? duration : 30;

        List<Appointment> existing = appointmentRepository
                .findByBarberIdAndAppointmentDateAndStatusInOrderByStartTimeAsc(barberId, date, ACTIVE_STATUSES);

        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();
        LocalTime cursor = open;
        java.util.ArrayList<AvailableSlotResponse> slots = new java.util.ArrayList<>();
        while (!cursor.plusMinutes(duration).isAfter(close)) {
            LocalTime start = cursor;
            LocalTime end = cursor.plusMinutes(duration);
            boolean past = date.isBefore(today) || (date.equals(today) && start.isBefore(now));
            boolean conflict = existing.stream().anyMatch(a -> start.isBefore(a.getEndTime()) && end.isAfter(a.getStartTime()));
            slots.add(new AvailableSlotResponse(start.format(TIME_FORMAT), end.format(TIME_FORMAT), !past && !conflict));
            cursor = cursor.plusMinutes(step);
        }
        return slots;
    }

    @Transactional
    public AppointmentResponse create(CreateAppointmentRequest request) {
        Long shopId = request.shopId() == null ? 1L : request.shopId();
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shop not found"));
        Barber barber = barberRepository.findById(required(request.barberId(), "barberId"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Barber not found"));
        ServiceItem service = serviceItemRepository.findById(required(request.serviceId(), "serviceId"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));
        User user = resolveUser(request);

        LocalDate date = LocalDate.parse(required(request.appointmentDate(), "appointmentDate"), DATE_FORMAT);
        LocalTime start = LocalTime.parse(required(request.startTime(), "startTime"));
        LocalTime end = start.plusMinutes(service.getDurationMinutes() == null ? 45 : service.getDurationMinutes());
        if (date.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment date is in the past");
        }
        if (appointmentRepository.existsTimeConflict(barber.getId(), date, start, end, ACTIVE_STATUSES)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Selected time slot is unavailable");
        }

        Appointment appointment = new Appointment();
        appointment.setAppointmentNo("AP" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        appointment.setUser(user);
        appointment.setShop(shop);
        appointment.setBarber(barber);
        appointment.setService(service);
        appointment.setAppointmentDate(date);
        appointment.setStartTime(start);
        appointment.setEndTime(end);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setCustomerName(required(request.customerName(), "customerName"));
        appointment.setCustomerPhone(required(request.customerPhone(), "customerPhone"));
        appointment.setRemark(request.remark());
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> myAppointments(String openid) {
        User user = userRepository.findByOpenid(normalizeOpenid(openid))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return appointmentRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    @Transactional
    public AppointmentResponse cancel(Long id, CancelAppointmentRequest request, String openid) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        String expectedOpenid = normalizeOpenid(openid);
        if (!appointment.getUser().getOpenid().equals(expectedOpenid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot cancel another user's appointment");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment cannot be cancelled");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelReason(request == null ? null : request.reason());
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    private User resolveUser(CreateAppointmentRequest request) {
        String openid = normalizeOpenid(request.openid());
        return userRepository.findByOpenid(openid).orElseGet(() -> {
            User user = new User();
            user.setOpenid(openid);
            user.setNickname("微信用户");
            user.setPhone(request.customerPhone());
            return userRepository.save(user);
        });
    }

    private String normalizeOpenid(String openid) {
        return StringUtils.hasText(openid) ? openid.trim() : "dev-openid";
    }

    private static <T> T required(T value, String field) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        if (value instanceof String s && !StringUtils.hasText(s)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return value;
    }

    public record AvailableSlotResponse(String startTime, String endTime, boolean available) {}

    public record CreateAppointmentRequest(
            String openid,
            Long shopId,
            Long barberId,
            Long serviceId,
            String appointmentDate,
            String startTime,
            String customerName,
            String customerPhone,
            String remark
    ) {}

    public record CancelAppointmentRequest(String reason) {}

    public record AppointmentResponse(
            Long id,
            String appointmentNo,
            Long shopId,
            String shopName,
            Long barberId,
            String barberName,
            Long serviceId,
            String serviceName,
            Integer priceCents,
            Integer price,
            String appointmentDate,
            String startTime,
            String endTime,
            String status,
            String customerName,
            String customerPhone,
            String remark,
            String cancelReason
    ) {
        static AppointmentResponse from(Appointment appointment) {
            return new AppointmentResponse(
                    appointment.getId(),
                    appointment.getAppointmentNo(),
                    appointment.getShop().getId(),
                    appointment.getShop().getName(),
                    appointment.getBarber().getId(),
                    appointment.getBarber().getName(),
                    appointment.getService().getId(),
                    appointment.getService().getName(),
                    appointment.getService().getPriceCents(),
                    appointment.getService().getPriceCents() == null ? null : appointment.getService().getPriceCents() / 100,
                    appointment.getAppointmentDate().format(DATE_FORMAT),
                    appointment.getStartTime().format(TIME_FORMAT),
                    appointment.getEndTime().format(TIME_FORMAT),
                    appointment.getStatus().name(),
                    appointment.getCustomerName(),
                    appointment.getCustomerPhone(),
                    appointment.getRemark(),
                    appointment.getCancelReason()
            );
        }
    }
}
