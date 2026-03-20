package com.cgi.reservation.repository;

import com.cgi.reservation.model.Reservation;
import com.cgi.reservation.model.ReservationStatus;
import com.cgi.reservation.model.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.status = :status " +
           "AND r.startTime < :endTime " +
           "AND FUNCTION('DATEADD', 'MINUTE', r.durationMinutes, r.startTime) > :startTime")
    List<Reservation> findOverlapping(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("status") ReservationStatus status);

    @Query("SELECT r FROM Reservation r WHERE r.table = :table " +
           "AND r.status = 'CONFIRMED' " +
           "AND r.startTime < :endTime " +
           "AND FUNCTION('DATEADD', 'MINUTE', r.durationMinutes, r.startTime) > :startTime")
    List<Reservation> findOverlappingForTable(
            @Param("table") RestaurantTable table,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    List<Reservation> findByStartTimeBetweenAndStatus(
            LocalDateTime start, LocalDateTime end, ReservationStatus status);
}
