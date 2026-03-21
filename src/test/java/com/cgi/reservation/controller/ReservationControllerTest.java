package com.cgi.reservation.controller;

import com.cgi.reservation.dto.ReservationRequest;
import com.cgi.reservation.dto.ReservationResponse;
import com.cgi.reservation.model.ReservationStatus;
import com.cgi.reservation.model.Zone;
import com.cgi.reservation.service.ReservationService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    @Test
    void createReservation_returns201() throws Exception {
        ReservationResponse response = new ReservationResponse();
        response.setId(1L);
        response.setTableNumber(5);
        response.setTableZone(Zone.INDOOR_MAIN);
        response.setTableCapacity(4);
        response.setCustomerName("Mari Mets");
        response.setPartySize(2);
        response.setStartTime(LocalDateTime.of(2026, 3, 25, 19, 0));
        response.setEndTime(LocalDateTime.of(2026, 3, 25, 21, 0));
        response.setDurationMinutes(120);
        response.setStatus(ReservationStatus.CONFIRMED);

        when(reservationService.createReservation(any(ReservationRequest.class)))
                .thenReturn(response);

        ReservationRequest request = new ReservationRequest();
        request.setTableId(5L);
        request.setCustomerName("Mari Mets");
        request.setPartySize(2);
        request.setDateTime(LocalDateTime.of(2026, 3, 25, 19, 0));
        request.setDurationMinutes(120);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.customerName").value("Mari Mets"))
                .andExpect(jsonPath("$.tableNumber").value(5));
    }

    @Test
    void createReservation_conflict_returns409() throws Exception {
        when(reservationService.createReservation(any(ReservationRequest.class)))
                .thenThrow(new IllegalStateException("Laud on juba broneeritud valitud ajal"));

        ReservationRequest request = new ReservationRequest();
        request.setTableId(5L);
        request.setCustomerName("Jaan Tamm");
        request.setPartySize(2);
        request.setDateTime(LocalDateTime.of(2026, 3, 25, 19, 0));

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Laud on juba broneeritud valitud ajal"));
    }

    @Test
    void cancelReservation_returns204() throws Exception {
        doNothing().when(reservationService).cancelReservation(1L);

        mockMvc.perform(delete("/api/reservations/1"))
                .andExpect(status().isNoContent());

        verify(reservationService).cancelReservation(1L);
    }
}
