package com.cgi.reservation.dto;

import com.cgi.reservation.model.ReservationStatus;
import com.cgi.reservation.model.Zone;

import java.time.LocalDateTime;

public class ReservationResponse {

    private Long id;
    private int tableNumber;
    private Zone tableZone;
    private int tableCapacity;
    private String customerName;
    private int partySize;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int durationMinutes;
    private ReservationStatus status;
    private String preferences;
    private LocalDateTime createdAt;

    public ReservationResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getTableNumber() { return tableNumber; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }

    public Zone getTableZone() { return tableZone; }
    public void setTableZone(Zone tableZone) { this.tableZone = tableZone; }

    public int getTableCapacity() { return tableCapacity; }
    public void setTableCapacity(int tableCapacity) { this.tableCapacity = tableCapacity; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public int getPartySize() { return partySize; }
    public void setPartySize(int partySize) { this.partySize = partySize; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
