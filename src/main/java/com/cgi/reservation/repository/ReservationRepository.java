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

    @Query(value = "SELECT r.* FROM reservation r WHERE r.status = :status " +
           "AND r.start_time < :endTime " +
           "AND DATEADD('MINUTE', r.duration_minutes, r.start_time) > :startTime",
           nativeQuery = true)
    List<Reservation> findOverlapping(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("status") String status);

    @Query(value = "SELECT r.* FROM reservation r WHERE r.table_id = :tableId " +
           "AND r.status = 'CONFIRMED' " +
           "AND r.start_time < :endTime " +
           "AND DATEADD('MINUTE', r.duration_minutes, r.start_time) > :startTime",
           nativeQuery = true)
    List<Reservation> findOverlappingForTable(
            @Param("tableId") Long tableId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    List<Reservation> findByStartTimeBetweenAndStatus(
            LocalDateTime start, LocalDateTime end, ReservationStatus status);
}
