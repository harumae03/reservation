package com.cgi.reservation.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class ReservationRequest {

    @NotNull(message = "Laua ID on kohustuslik")
    private Long tableId;

    @NotBlank(message = "Kliendi nimi on kohustuslik")
    private String customerName;

    @Min(value = 1, message = "Seltskonna suurus peab olema vähemalt 1")
    @Max(value = 20, message = "Seltskonna suurus ei tohi ületada 20")
    private int partySize;

    @NotNull(message = "Broneeringu aeg on kohustuslik")
    @FutureOrPresent(message = "Broneeringu aeg ei tohi olla minevikus")
    private LocalDateTime dateTime;

    @Min(value = 30, message = "Kestus peab olema vähemalt 30 minutit")
    @Max(value = 360, message = "Kestus ei tohi ületada 6 tundi")
    private int durationMinutes = 120;

    private String preferences;

    public ReservationRequest() {}

    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public int getPartySize() { return partySize; }
    public void setPartySize(int partySize) { this.partySize = partySize; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }
}
