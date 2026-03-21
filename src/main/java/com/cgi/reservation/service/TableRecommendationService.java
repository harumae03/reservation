package com.cgi.reservation.service;

import com.cgi.reservation.dto.TableRecommendationDTO;
import com.cgi.reservation.model.RestaurantTable;
import com.cgi.reservation.model.Zone;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

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

        // Sort: preference-matching first, then zone-matching, then by score descending
        scored.sort((a, b) -> {
            // Preference-matching tables first (when preferences selected)
            if (preferences != null && !preferences.isEmpty()) {
                boolean aPref = hasAnyPreference(a, preferences);
                boolean bPref = hasAnyPreference(b, preferences);
                if (aPref != bPref) return aPref ? -1 : 1;
            }
            // Zone-matching tables next (when zone selected)
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

    // Playground area center coordinates (matching FloorPlan SVG)
    private static final double PLAYGROUND_X = 170;
    private static final double PLAYGROUND_Y = 640;
    private static final double PLAYGROUND_MAX_DIST = 450;

    /**
     * Preference score (0-25): uses proximity-based scoring for spatial preferences.
     * Tables with the exact flag get full match (1.0), nearby tables get partial credit
     * based on distance. If no preferences requested, returns 10 (neutral).
     */
    int calculatePreferenceScore(RestaurantTable table, List<String> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            return 10;
        }

        double totalMatch = 0;
        for (String pref : preferences) {
            switch (pref.toLowerCase()) {
                case "window", "bywindow", "by_window" -> {
                    if (table.isByWindow()) totalMatch += 1.0;
                }
                case "quiet", "privacy" -> {
                    if (table.isQuiet()) totalMatch += 1.0;
                }
                case "playground", "nearplayground", "near_playground" -> {
                    if (table.isNearPlayground()) {
                        totalMatch += 1.0;
                    } else {
                        totalMatch += playgroundProximity(table);
                    }
                }
            }
        }

        return (int) Math.round(totalMatch / preferences.size() * 25);
    }

    /**
     * Returns 0.0–0.6 based on distance to playground area.
     * Closer tables get higher partial credit.
     */
    private double playgroundProximity(RestaurantTable table) {
        double cx = table.getPosX() + table.getWidth() / 2.0;
        double cy = table.getPosY() + table.getHeight() / 2.0;
        double dist = Math.sqrt(Math.pow(cx - PLAYGROUND_X, 2) + Math.pow(cy - PLAYGROUND_Y, 2));
        if (dist >= PLAYGROUND_MAX_DIST) return 0.0;
        return 0.6 * (1.0 - dist / PLAYGROUND_MAX_DIST);
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

    private boolean hasAnyPreference(TableRecommendationDTO dto, List<String> preferences) {
        for (String pref : preferences) {
            switch (pref.toLowerCase()) {
                case "window", "bywindow", "by_window" -> { if (dto.isByWindow()) return true; }
                case "quiet", "privacy" -> { if (dto.isQuiet()) return true; }
                case "playground", "nearplayground", "near_playground" -> { if (dto.isNearPlayground()) return true; }
            }
        }
        return false;
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
