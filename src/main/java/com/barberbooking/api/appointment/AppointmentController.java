package com.barberbooking.api.appointment;

import java.time.LocalDate;
import java.util.List;

import com.barberbooking.api.appointment.AppointmentService.AppointmentResponse;
import com.barberbooking.api.appointment.AppointmentService.AvailableSlotResponse;
import com.barberbooking.api.appointment.AppointmentService.CancelAppointmentRequest;
import com.barberbooking.api.appointment.AppointmentService.CreateAppointmentRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/available-slots")
    public List<AvailableSlotResponse> availableSlots(
            @RequestParam Long barberId,
            @RequestParam Long serviceId,
            @RequestParam String date
    ) {
        return appointmentService.availableSlots(barberId, serviceId, LocalDate.parse(date));
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.create(request));
    }

    @GetMapping("/my")
    public List<AppointmentResponse> my(@RequestParam(defaultValue = "dev-openid") String openid) {
        return appointmentService.myAppointments(openid);
    }

    @PostMapping("/{id}/cancel")
    public AppointmentResponse cancel(
            @PathVariable Long id,
            @RequestParam(defaultValue = "dev-openid") String openid,
            @RequestBody(required = false) CancelAppointmentRequest request
    ) {
        return appointmentService.cancel(id, request, openid);
    }
}
