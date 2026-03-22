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
    // Maximum distance (SVG units) between table centers to consider them adjacent
    private static final double MERGE_MAX_DISTANCE = 220.0;

    public List<TableRecommendationDTO> recommend(LocalDateTime dateTime, int partySize,
                                                   int durationMinutes, Zone zone,
                                                   List<String> preferences) {
        List<RestaurantTable> allAvailable = availabilityService.getAvailableTables(dateTime, durationMinutes);

        // Filter out tables that are too small for the party
        List<RestaurantTable> available = allAvailable.stream()
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

        // If no single table fits, try merging adjacent tables
        if (scored.isEmpty() && partySize > 1) {
            scored = recommendMerged(allAvailable, partySize, zone, preferences);
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
     * Finds pairs of adjacent, same-zone, available tables whose combined capacity
     * meets the party size. Used when no single table is large enough.
     */
    private List<TableRecommendationDTO> recommendMerged(List<RestaurantTable> available,
                                                          int partySize, Zone zone,
                                                          List<String> preferences) {
        List<TableRecommendationDTO> merged = new ArrayList<>();

        for (int i = 0; i < available.size(); i++) {
            for (int j = i + 1; j < available.size(); j++) {
                RestaurantTable t1 = available.get(i);
                RestaurantTable t2 = available.get(j);

                // Must be same zone
                if (t1.getZone() != t2.getZone()) continue;

                // Combined capacity must fit the party
                int combined = t1.getCapacity() + t2.getCapacity();
                if (combined < partySize) continue;

                // Must be adjacent (close enough to push together)
                double dist = tableCenterDistance(t1, t2);
                if (dist > MERGE_MAX_DISTANCE) continue;

                // Score the pair
                int extra = combined - partySize;
                int capacityScore = Math.max(40 - (extra * 10), 0);
                int prefScore = (calculatePreferenceScore(t1, preferences)
                        + calculatePreferenceScore(t2, preferences)) / 2;
                int zoneScore = calculateZoneScore(t1, zone);
                int totalScore = capacityScore + prefScore + zoneScore;

                // Use midpoint position for display
                double midX = Math.min(t1.getPosX(), t2.getPosX());
                double midY = Math.min(t1.getPosY(), t2.getPosY());

                String reason = "Liidus: Laud " + t1.getTableNumber() + " + Laud "
                        + t2.getTableNumber() + " (" + combined + " kohta kokku)";

                TableRecommendationDTO dto = new TableRecommendationDTO(
                        t1.getId(), t1.getTableNumber(), combined, t1.getZone(),
                        midX, midY, t1.getWidth(), t1.getHeight(),
                        t1.isByWindow() || t2.isByWindow(),
                        t1.isQuiet() || t2.isQuiet(),
                        t1.isNearPlayground() || t2.isNearPlayground(),
                        totalScore, 0, reason);
                dto.setMerged(true);
                dto.setMergedTableIds(List.of(t1.getId(), t2.getId()));
                dto.setMergedTableNumbers(List.of(t1.getTableNumber(), t2.getTableNumber()));

                merged.add(dto);
            }
        }

        return merged;
    }

    private double tableCenterDistance(RestaurantTable t1, RestaurantTable t2) {
        double cx1 = t1.getPosX() + t1.getWidth() / 2.0;
        double cy1 = t1.getPosY() + t1.getHeight() / 2.0;
        double cx2 = t2.getPosX() + t2.getWidth() / 2.0;
        double cy2 = t2.getPosY() + t2.getHeight() / 2.0;
        return Math.sqrt(Math.pow(cx1 - cx2, 2) + Math.pow(cy1 - cy2, 2));
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
