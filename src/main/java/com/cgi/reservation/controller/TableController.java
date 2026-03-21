package com.cgi.reservation.controller;

import com.cgi.reservation.dto.TableRecommendationDTO;
import com.cgi.reservation.dto.TableWithStatusDTO;
import com.cgi.reservation.model.RestaurantTable;
import com.cgi.reservation.model.Zone;
import com.cgi.reservation.repository.RestaurantTableRepository;
import com.cgi.reservation.service.AvailabilityService;
import com.cgi.reservation.service.TableRecommendationService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    private final RestaurantTableRepository tableRepository;
    private final AvailabilityService availabilityService;
    private final TableRecommendationService recommendationService;

    public TableController(RestaurantTableRepository tableRepository,
                           AvailabilityService availabilityService,
                           TableRecommendationService recommendationService) {
        this.tableRepository = tableRepository;
        this.availabilityService = availabilityService;
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAll();
    }

    @GetMapping("/status")
    public List<TableWithStatusDTO> getTableStatuses(
            @RequestParam LocalDateTime dateTime,
            @RequestParam(defaultValue = "120") int duration) {
        return availabilityService.getTableStatuses(dateTime, duration);
    }

    @GetMapping("/recommend")
    public List<TableRecommendationDTO> getRecommendations(
            @RequestParam LocalDateTime dateTime,
            @RequestParam int partySize,
            @RequestParam(defaultValue = "120") int duration,
            @RequestParam(required = false) Zone zone,
            @RequestParam(required = false) List<String> preferences) {
        return recommendationService.recommend(dateTime, partySize, duration, zone, preferences);
    }
}
