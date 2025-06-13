package com.example.demo.plant.service;

import com.example.demo.plant.dao.NutrientDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NutrientService {

    private final NutrientDao nutrientDao;

    public int getNutrientStock(Long fid) {
        return nutrientDao.getNutrientStock(fid);
    }
}
