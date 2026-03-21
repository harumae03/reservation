package com.cgi.reservation.service;

import com.cgi.reservation.dto.TableRecommendationDTO;
import com.cgi.reservation.model.RestaurantTable;
import com.cgi.reservation.model.Zone;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TableRecommendationService {

    private final AvailabilityService availabilityService;

    public TableRecommendationService(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    /**
     * Scores and ranks available tables based on party size, preferences, and zone.
     * When a zone is selected, matching tables are always ranked above non-matching ones.
     * Returns top recommendations sorted by score descending.
     */
    public List<TableRecommendationDTO> recommend(LocalDateTime dateTime, int partySize,
                                                   int durationMinutes, Zone zone,
                                                   List<String> preferences) {
        List<RestaurantTable> available = availabilityService.getAvailableTables(dateTime, durationMinutes);

        // Filter out tables that are too small for the party
        available = available.stream()
                .filter(t -> t.getCapacity() >= partySize)
                .collect(Collectors.toList());

        List<TableRecommendationDTO> scored = new ArrayList<>();
        for (RestaurantTable table : available) {
            int capacityScore = calculateCapacityScore(table, partySize);
            int preferenceScore = calculatePreferenceScore(table, preferences);
            int zoneScore = calculateZoneScore(table, zone);
            int totalScore = capacityScore + preferenceScore + zoneScore;
            String reason = buildReason(table, partySize, zone, preferences);

            scored.add(toDTO(table, totalScore, 0, reason));
        }

        // Sort: zone-matching tables first (when zone selected), then by score descending
        scored.sort((a, b) -> {
            if (zone != null) {
                boolean aMatch = a.getZone() == zone;
                boolean bMatch = b.getZone() == zone;
                if (aMatch != bMatch) return aMatch ? -1 : 1;
            }
            return Integer.compare(b.getScore(), a.getScore());
        });

        // Assign ranks
        for (int i = 0; i < scored.size(); i++) {
            scored.get(i).setRank(i + 1);
        }

        return scored;
    }

    /**
     * Capacity score (0-40): exact match = 40, each extra seat costs 10 points.
     */
    int calculateCapacityScore(RestaurantTable table, int partySize) {
        int extra = table.getCapacity() - partySize;
        int score = 40 - (extra * 10);
        return Math.max(score, 0);
    }

    /**
     * Preference score (0-25): normalized as (matched / requested) * 25.
     * If no preferences requested, returns 10 (neutral).
     */
    int calculatePreferenceScore(RestaurantTable table, List<String> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            return 10;
        }

        int matched = 0;
        for (String pref : preferences) {
            switch (pref.toLowerCase()) {
                case "window", "bywindow", "by_window" -> {
                    if (table.isByWindow()) matched++;
                }
                case "quiet", "privacy" -> {
                    if (table.isQuiet()) matched++;
                }
                case "playground", "nearplayground", "near_playground" -> {
                    if (table.isNearPlayground()) matched++;
                }
            }
        }

        return (int) Math.round((double) matched / preferences.size() * 25);
    }

    /**
     * Zone score (0-35): requested zone match = 35, no filter = 10.
     * Zone is weighted heavily so matching-zone tables always outscore non-matching ones.
     */
    int calculateZoneScore(RestaurantTable table, Zone zone) {
        if (zone == null) {
            return 10;
        }
        return table.getZone() == zone ? 35 : 0;
    }

    private String buildReason(RestaurantTable table, int partySize, Zone zone, List<String> preferences) {
        List<String> reasons = new ArrayList<>();

        if (table.getCapacity() == partySize) {
            reasons.add("Täpne kohtade arv (" + partySize + ")");
        } else {
            reasons.add(table.getCapacity() + "-kohaline laud " + partySize + " inimesele");
        }

        if (zone != null && table.getZone() == zone) {
            reasons.add("soovitud tsoon");
        }

        if (preferences != null) {
            if (preferences.stream().anyMatch(p -> p.equalsIgnoreCase("window") || p.equalsIgnoreCase("byWindow")) && table.isByWindow()) {
                reasons.add("akna ääres");
            }
            if (preferences.stream().anyMatch(p -> p.equalsIgnoreCase("quiet") || p.equalsIgnoreCase("privacy")) && table.isQuiet()) {
                reasons.add("vaikne nurk");
            }
            if (preferences.stream().anyMatch(p -> p.equalsIgnoreCase("playground") || p.equalsIgnoreCase("nearPlayground")) && table.isNearPlayground()) {
                reasons.add("mängunurga lähedal");
            }
        }

        return String.join(", ", reasons);
    }

    private TableRecommendationDTO toDTO(RestaurantTable table, int score, int rank, String reason) {
        return new TableRecommendationDTO(
                table.getId(), table.getTableNumber(), table.getCapacity(), table.getZone(),
                table.getPosX(), table.getPosY(), table.getWidth(), table.getHeight(),
                table.isByWindow(), table.isQuiet(), table.isNearPlayground(),
                score, rank, reason);
    }
}
