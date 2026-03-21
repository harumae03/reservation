package com.cgi.reservation.service;

import com.cgi.reservation.dto.TableWithStatusDTO;
import com.cgi.reservation.model.Reservation;
import com.cgi.reservation.model.ReservationStatus;
import com.cgi.reservation.model.RestaurantTable;
import com.cgi.reservation.repository.ReservationRepository;
import com.cgi.reservation.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private final RestaurantTableRepository tableRepository;
    private final ReservationRepository reservationRepository;

    public AvailabilityService(RestaurantTableRepository tableRepository,
                               ReservationRepository reservationRepository) {
        this.tableRepository = tableRepository;
        this.reservationRepository = reservationRepository;
    }

    /**
     * Returns tables that are NOT reserved during the given time window.
     */
    public List<RestaurantTable> getAvailableTables(LocalDateTime start, int durationMinutes) {
        LocalDateTime end = start.plusMinutes(durationMinutes);
        List<Reservation> overlapping = reservationRepository.findOverlapping(
                start, end, ReservationStatus.CONFIRMED);

        Set<Long> occupiedTableIds = overlapping.stream()
                .map(r -> r.getTable().getId())
                .collect(Collectors.toSet());

        return tableRepository.findAll().stream()
                .filter(t -> !occupiedTableIds.contains(t.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Returns ALL tables with their available/occupied status at the given time.
     * Used for rendering the floor plan.
     */
    public List<TableWithStatusDTO> getTableStatuses(LocalDateTime dateTime, int durationMinutes) {
        LocalDateTime end = dateTime.plusMinutes(durationMinutes);
        List<Reservation> overlapping = reservationRepository.findOverlapping(
                dateTime, end, ReservationStatus.CONFIRMED);

        Set<Long> occupiedTableIds = overlapping.stream()
                .map(r -> r.getTable().getId())
                .collect(Collectors.toSet());

        return tableRepository.findAll().stream()
                .map(table -> toDTO(table, occupiedTableIds.contains(table.getId()) ? "occupied" : "available"))
                .collect(Collectors.toList());
    }

    private TableWithStatusDTO toDTO(RestaurantTable table, String status) {
        return new TableWithStatusDTO(
                table.getId(), table.getTableNumber(), table.getCapacity(), table.getZone(),
                table.getPosX(), table.getPosY(), table.getWidth(), table.getHeight(),
                table.isByWindow(), table.isQuiet(), table.isNearPlayground(),
                status);
    }
}
