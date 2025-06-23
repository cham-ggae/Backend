package com.example.demo.plant.service;

import com.example.demo.plant.dao.NutrientDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import static com.example.demo.plant.exception.PlantExceptions.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NutrientService {

    private final NutrientDao nutrientDao;
    // 현재 영양제 재고 조회
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
    // 영양제 사용시 1개 차감
    public void useNutrient(Long uid) {
        Long fid = nutrientDao.getFamilyIdByUid(uid);
        log.debug("🎯 [Nutrient] fid: {}", fid);
        if (fid == null) {
            throw new NutrientStockNotFoundException("가족 정보가 존재하지 않습니다.");
        }

        Integer stock = nutrientDao.getNutrientStock(fid);
        if (stock == null || stock <= 0) {
            log.debug("🎯 [Nutrient] stock: {}", stock);
            throw new NutrientStockNotFoundException("사용 가능한 영양제가 없습니다.");
        }

        nutrientDao.decrementNutrientStock(fid);
        log.info("✅ 영양제 1개 차감 완료");
    }

}