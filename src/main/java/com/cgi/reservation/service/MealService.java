package com.cgi.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fetches daily meal specials from TheMealDB API.
 * Caches 3 random meals per day to avoid repeated API calls.
 */
@Service
public class MealService {

    private static final String RANDOM_MEAL_URL = "https://www.themealdb.com/api/json/v1/1/random.php";

    private final RestClient restClient = RestClient.create();
    private List<MealDTO> cachedMeals = List.of();
    private LocalDate cacheDate = null;

    public List<MealDTO> getDailySpecials() {
        LocalDate today = LocalDate.now();
        if (today.equals(cacheDate) && !cachedMeals.isEmpty()) {
            return cachedMeals;
        }

        List<MealDTO> meals = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            try {
                MealDTO meal = fetchRandomMeal();
                if (meal != null) {
                    meals.add(meal);
                }
            } catch (Exception e) {
                // Skip failed fetch, continue with others
            }
        }

        if (!meals.isEmpty()) {
            cachedMeals = List.copyOf(meals);
            cacheDate = today;
        }
        return cachedMeals;
    }

    @SuppressWarnings("unchecked")
    private MealDTO fetchRandomMeal() {
        Map<String, Object> response = restClient.get()
                .uri(RANDOM_MEAL_URL)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("meals") == null) {
            return null;
        }

        List<Map<String, Object>> meals = (List<Map<String, Object>>) response.get("meals");
        if (meals.isEmpty()) {
            return null;
        }

        Map<String, Object> meal = meals.getFirst();
        return new MealDTO(
                (String) meal.get("strMeal"),
                (String) meal.get("strCategory"),
                (String) meal.get("strArea"),
                (String) meal.get("strMealThumb")
        );
    }

    public record MealDTO(String name, String category, String area, String imageUrl) {}
}
