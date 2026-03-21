package com.cgi.reservation.controller;

import com.cgi.reservation.service.MealService;
import com.cgi.reservation.service.MealService.MealDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/daily-specials")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @GetMapping
    public List<MealDTO> getDailySpecials() {
        return mealService.getDailySpecials();
    }
}
