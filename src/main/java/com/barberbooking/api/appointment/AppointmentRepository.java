package com.barberbooking.api.appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Appointment> findByBarberIdAndAppointmentDateAndStatusInOrderByStartTimeAsc(
            Long barberId,
            LocalDate appointmentDate,
            Collection<AppointmentStatus> statuses
    );

    @Query("""
            select count(a) > 0
            from Appointment a
            where a.barber.id = :barberId
              and a.appointmentDate = :appointmentDate
              and a.status in :activeStatuses
              and a.startTime < :endTime
              and a.endTime > :startTime
            """)
    boolean existsTimeConflict(
            @Param("barberId") Long barberId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("activeStatuses") Collection<AppointmentStatus> activeStatuses
    );
}
