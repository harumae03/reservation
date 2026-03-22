package com.cgi.reservation.dto;

import com.cgi.reservation.model.Zone;

import java.time.LocalDateTime;

public class TableWithStatusDTO {

    private Long id;
    private int tableNumber;
    private int capacity;
    private Zone zone;
    private double posX;
    private double posY;
    private double width;
    private double height;
    private boolean byWindow;
    private boolean quiet;
    private boolean nearPlayground;
    private String status; // "available" or "occupied"

    // Reservation details (populated when occupied)
    private String customerName;
    private LocalDateTime reservationStart;
    private LocalDateTime reservationEnd;
    private int partySize;

    public TableWithStatusDTO() {}

    public TableWithStatusDTO(Long id, int tableNumber, int capacity, Zone zone,
                              double posX, double posY, double width, double height,
                              boolean byWindow, boolean quiet, boolean nearPlayground,
                              String status) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.zone = zone;
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        this.byWindow = byWindow;
        this.quiet = quiet;
        this.nearPlayground = nearPlayground;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getTableNumber() { return tableNumber; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public Zone getZone() { return zone; }
    public void setZone(Zone zone) { this.zone = zone; }

    public double getPosX() { return posX; }
    public void setPosX(double posX) { this.posX = posX; }

    public double getPosY() { return posY; }
    public void setPosY(double posY) { this.posY = posY; }

    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public boolean isByWindow() { return byWindow; }
    public void setByWindow(boolean byWindow) { this.byWindow = byWindow; }

    public boolean isQuiet() { return quiet; }
    public void setQuiet(boolean quiet) { this.quiet = quiet; }

    public boolean isNearPlayground() { return nearPlayground; }
    public void setNearPlayground(boolean nearPlayground) { this.nearPlayground = nearPlayground; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public LocalDateTime getReservationStart() { return reservationStart; }
    public void setReservationStart(LocalDateTime reservationStart) { this.reservationStart = reservationStart; }

    public LocalDateTime getReservationEnd() { return reservationEnd; }
    public void setReservationEnd(LocalDateTime reservationEnd) { this.reservationEnd = reservationEnd; }

    public int getPartySize() { return partySize; }
    public void setPartySize(int partySize) { this.partySize = partySize; }
}
