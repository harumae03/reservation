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
        // Determine which table IDs to book
        List<Long> idsToBook = resolveTableIds(request);

        // Validate opening hours once
        LocalDateTime start = request.getDateTime();
        int startHour = start.getHour();
        LocalDateTime end = start.plusMinutes(request.getDurationMinutes());
        int endHour = end.getHour();
        int endMinute = end.getMinute();

        if (startHour < 11 || startHour >= 22) {
            throw new IllegalArgumentException("Restoran on avatud 11:00–22:00");
        }
        if (endHour > 22 || (endHour == 22 && endMinute > 0)) {
            throw new IllegalArgumentException("Broneering ei tohi ületada sulgemisaega (22:00)");
        }

        // Book each table
        Reservation firstReservation = null;
        int totalCapacity = 0;

        for (Long tableId : idsToBook) {
            RestaurantTable table = tableRepository.findById(tableId)
                    .orElseThrow(() -> new IllegalArgumentException("Lauda ei leitud ID-ga: " + tableId));
            totalCapacity += table.getCapacity();

            // Check for time overlap on this table
            List<Reservation> overlapping = reservationRepository.findOverlappingForTable(table.getId(), start, end);
            if (!overlapping.isEmpty()) {
                throw new IllegalStateException("Laud " + table.getTableNumber() + " on juba broneeritud valitud ajal");
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

            if (firstReservation == null) firstReservation = reservation;
        }

        // Validate party size vs total capacity (single or merged)
        if (request.getPartySize() > totalCapacity) {
            throw new IllegalArgumentException("Seltskond (" + request.getPartySize()
                    + ") on suurem kui laudade mahutavus (" + totalCapacity + ")");
        }

        return toResponse(firstReservation);
    }

    private List<Long> resolveTableIds(ReservationRequest request) {
        if (request.getTableIds() != null && !request.getTableIds().isEmpty()) {
            return request.getTableIds();
        }
        if (request.getTableId() != null) {
            return List.of(request.getTableId());
        }
        throw new IllegalArgumentException("Laua ID on kohustuslik");
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
