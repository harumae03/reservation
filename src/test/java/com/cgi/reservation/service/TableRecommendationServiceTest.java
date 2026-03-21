package com.cgi.reservation.service;

import com.cgi.reservation.dto.TableRecommendationDTO;
import com.cgi.reservation.model.RestaurantTable;
import com.cgi.reservation.model.Zone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableRecommendationServiceTest {

    @Mock
    private AvailabilityService availabilityService;

    private TableRecommendationService service;

    private RestaurantTable table2seat;
    private RestaurantTable table4seat;
    private RestaurantTable table8seat;
    private RestaurantTable windowTable;
    private RestaurantTable quietTable;

    @BeforeEach
    void setUp() {
        service = new TableRecommendationService(availabilityService);

        table2seat = new RestaurantTable(1, 2, Zone.INDOOR_MAIN,
                100, 100, 60, 60, false, false, false);
        table2seat.setId(1L);

        table4seat = new RestaurantTable(2, 4, Zone.INDOOR_MAIN,
                200, 100, 80, 80, false, false, false);
        table4seat.setId(2L);

        table8seat = new RestaurantTable(3, 8, Zone.TERRACE,
                300, 100, 100, 60, false, false, false);
        table8seat.setId(3L);

        windowTable = new RestaurantTable(4, 2, Zone.INDOOR_WINDOW,
                400, 100, 60, 60, true, true, false);
        windowTable.setId(4L);

        quietTable = new RestaurantTable(5, 4, Zone.PRIVATE_ROOM,
                500, 100, 80, 80, false, true, false);
        quietTable.setId(5L);
    }

    @Test
    void exactCapacityMatch_getsHighestScore() {
        when(availabilityService.getAvailableTables(any(), anyInt()))
                .thenReturn(List.of(table2seat, table4seat, table8seat));

        List<TableRecommendationDTO> results = service.recommend(
                LocalDateTime.now().plusHours(1), 2, 120, null, null);

        assertEquals(3, results.size()); // all 3 have capacity >= 2
        // The 2-seat table should rank first (exact match = 40 capacity score)
        assertEquals(1, results.get(0).getTableNumber());
        assertTrue(results.get(0).getScore() > results.get(1).getScore());
    }

    @Test
    void extraSeats_lowerScore() {
        // 2-seat table for 2 people = 40 points (exact match)
        int exactScore = service.calculateCapacityScore(table2seat, 2);
        // 4-seat table for 2 people = 40 - 20 = 20 points
        int extraScore = service.calculateCapacityScore(table4seat, 2);
        // 8-seat table for 2 people = 40 - 60 = 0 points (clamped)
        int bigExtraScore = service.calculateCapacityScore(table8seat, 2);

        assertEquals(40, exactScore);
        assertEquals(20, extraScore);
        assertEquals(0, bigExtraScore);
    }

    @Test
    void normalizedPreferenceScoring() {
        // Request 1 preference, table has it → 25/25
        int score1of1 = service.calculatePreferenceScore(windowTable, List.of("window"));
        assertEquals(25, score1of1);

        // Request 2 preferences, table has 1 → 13/25 (rounded)
        int score1of2 = service.calculatePreferenceScore(windowTable, List.of("window", "playground"));
        assertEquals(13, score1of2);

        // Request 3 preferences, table has 2 → 17/25 (rounded)
        int score2of3 = service.calculatePreferenceScore(windowTable, List.of("window", "quiet", "playground"));
        assertEquals(17, score2of3);

        // No preferences → neutral 10
        int noPrefs = service.calculatePreferenceScore(windowTable, null);
        assertEquals(10, noPrefs);
    }

    @Test
    void zoneScoring() {
        // Matching zone = 35
        assertEquals(35, service.calculateZoneScore(table2seat, Zone.INDOOR_MAIN));
        // Non-matching zone = 0
        assertEquals(0, service.calculateZoneScore(table2seat, Zone.TERRACE));
        // No zone filter = 10
        assertEquals(10, service.calculateZoneScore(table2seat, null));
    }

    @Test
    void emptyResult_whenAllTablesOccupied() {
        when(availabilityService.getAvailableTables(any(), anyInt()))
                .thenReturn(List.of());

        List<TableRecommendationDTO> results = service.recommend(
                LocalDateTime.now().plusHours(1), 2, 120, null, null);

        assertTrue(results.isEmpty());
    }

    @Test
    void preferenceAndZone_combinedScoring() {
        when(availabilityService.getAvailableTables(any(), anyInt()))
                .thenReturn(List.of(table2seat, windowTable));

        List<TableRecommendationDTO> results = service.recommend(
                LocalDateTime.now().plusHours(1), 2, 120,
                Zone.INDOOR_WINDOW, List.of("window", "quiet"));

        // windowTable should rank first: exact capacity (40) + 2/2 prefs (25) + zone match (35) = 100
        // table2seat: exact capacity (40) + 0/2 prefs (0) + no zone match (0) = 40
        assertEquals(4, results.get(0).getTableNumber());
        assertEquals(100, results.get(0).getScore());
        assertEquals(1, results.get(0).getRank());
    }
}
