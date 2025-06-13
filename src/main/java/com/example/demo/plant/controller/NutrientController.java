package com.example.demo.plant.controller;

import com.example.demo.plant.service.NutrientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nutrients")
@RequiredArgsConstructor
public class NutrientController {

    private final NutrientService nutrientService;

    @GetMapping("/stock/{fid}")
    public ResponseEntity<Integer> getNutrientStock(@PathVariable Long fid) {
        int stock = nutrientService.getNutrientStock(fid);
        return ResponseEntity.ok(stock);
    }
}