package com.example.demo.plant.service;

import com.example.demo.plant.dao.NutrientDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import static com.example.demo.plant.exception.PlantExceptions.*;


@Service
@RequiredArgsConstructor
public class NutrientService {

    private final NutrientDao nutrientDao;

    public int getNutrientStockByUid(Long uid) {
        Long fid = nutrientDao.getFamilyIdByUid(uid);
        if (fid == null) {
            throw new NutrientStockNotFoundException("가족 정보가 존재하지 않습니다.");
        }

        Integer stock = nutrientDao.getNutrientStock(fid);
        if (stock == null) {
            throw new NutrientStockNotFoundException("해당 가족의 영양제 수량을 찾을 수 없습니다.");
        }

        return stock;
    }
}