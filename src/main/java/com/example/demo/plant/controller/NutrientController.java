package com.example.demo.plant.controller;

import com.example.demo.login.service.AuthenticationService;
import com.example.demo.plant.service.NutrientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nutrients")
@RequiredArgsConstructor
public class NutrientController {

    private final NutrientService nutrientService;
    private final AuthenticationService authService;

    @GetMapping("/stock")
    public ResponseEntity<Integer> getNutrientStock() {
        Long uid = authService.getCurrentUserId();
        int stock = nutrientService.getNutrientStockByUid(uid);
        return ResponseEntity.ok(stock);
    }
}