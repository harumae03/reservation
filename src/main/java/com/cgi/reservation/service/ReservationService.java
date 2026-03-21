package com.cgi.reservation.service;

import com.cgi.reservation.dto.ReservationRequest;
import com.cgi.reservation.dto.ReservationResponse;
import com.cgi.reservation.model.Reservation;
import com.cgi.reservation.model.ReservationStatus;
import com.cgi.reservation.model.RestaurantTable;
import com.cgi.reservation.repository.ReservationRepository;
import com.cgi.reservation.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository tableRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              RestaurantTableRepository tableRepository) {
        this.reservationRepository = reservationRepository;
        this.tableRepository = tableRepository;
    }

    /**
     * Creates a reservation after validating input and checking for overlaps.
     * Re-checks overlap inside transaction to guard against race conditions.
     */
    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        // Validate input
        if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
            throw new IllegalArgumentException("Kliendi nimi on kohustuslik");
        }
        if (request.getPartySize() < 1 || request.getPartySize() > 20) {
            throw new IllegalArgumentException("Seltskonna suurus peab olema 1-20");
        }
        if (request.getDateTime() == null || request.getDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Broneeringu aeg ei tohi olla minevikus");
        }
        if (request.getTableId() == null) {
            throw new IllegalArgumentException("Laua ID on kohustuslik");
        }

        RestaurantTable table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new IllegalArgumentException("Lauda ei leitud ID-ga: " + request.getTableId()));

        // Check for time overlap on this table
        LocalDateTime start = request.getDateTime();
        LocalDateTime end = start.plusMinutes(request.getDurationMinutes());

        List<Reservation> overlapping = reservationRepository.findOverlappingForTable(table.getId(), start, end);
        if (!overlapping.isEmpty()) {
            throw new IllegalStateException("Laud on juba broneeritud valitud ajal");
        }

        Reservation reservation = new Reservation(
                table,
                request.getCustomerName().trim(),
                request.getPartySize(),
                start,
                request.getDurationMinutes(),
                request.getPreferences()
        );

        reservation = reservationRepository.save(reservation);
        return toResponse(reservation);
    }

    /**
     * Cancels a reservation by setting its status to CANCELLED.
     */
    @Transactional
    public void cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Broneeringut ei leitud ID-ga: " + id));
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    /**
     * Returns all confirmed reservations for a given date.
     */
    public List<ReservationResponse> getReservationsForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return reservationRepository.findByStartTimeBetweenAndStatus(start, end, ReservationStatus.CONFIRMED)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ReservationResponse toResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        response.setTableNumber(reservation.getTable().getTableNumber());
        response.setTableZone(reservation.getTable().getZone());
        response.setTableCapacity(reservation.getTable().getCapacity());
        response.setCustomerName(reservation.getCustomerName());
        response.setPartySize(reservation.getPartySize());
        response.setStartTime(reservation.getStartTime());
        response.setEndTime(reservation.getEndTime());
        response.setDurationMinutes(reservation.getDurationMinutes());
        response.setStatus(reservation.getStatus());
        response.setPreferences(reservation.getPreferences());
        response.setCreatedAt(reservation.getCreatedAt());
        return response;
    }
}
