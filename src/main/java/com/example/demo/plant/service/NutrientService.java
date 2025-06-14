package com.example.demo.plant.service;

import com.example.demo.plant.dao.NutrientDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NutrientService {

    private final NutrientDao nutrientDao;

    public int getNutrientStockByUid(Long uid) {
        Long fid = nutrientDao.getFamilyIdByUid(uid);
        if (fid == null) throw new RuntimeException("가족 정보가 존재하지 않습니다.");
        return nutrientDao.getNutrientStock(fid);
    }
}