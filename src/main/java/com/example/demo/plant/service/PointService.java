package com.example.demo.plant.service;

import com.example.demo.plant.dao.PointDao;
import com.example.demo.plant.dto.AddPointRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointDao pointDao;

    // 활동 적용 시 자동완료를 위한 메서드
    public boolean checkActivityExists(Long uid, String type) {
        return pointDao.checkActivityExists(uid, type);
    }

    // 활동 유형별로 부여할 포인트 값을 매핑
    private final Map<String, Integer> activityPointMap = Map.of(
            "attendance", 5,
            "water", 5,
            "nutrient", 10,
            "emotion", 10,
            "quiz", 10,
            "lastleaf", 10,
            "register", 10,
            "survey", 5
    );
    //활동에 따른 포인트 적립 및 경험치 처리
    @Transactional
    public void addPoint(AddPointRequestDto dto) {
        Long uid = dto.getUid();
        String type = dto.getActivityType();
        // 활동 1일 1회 제한
        if (pointDao.checkActivityExists(uid, type)) {
            throw new RuntimeException("오늘 이미 이 활동을 완료했습니다.");
        }
        //사용자 ID → 가족 ID, 식물 ID 조회
        Long fid = pointDao.getFamilyIdByUid(uid);
        Long pid = pointDao.getPlantIdByFid(fid);

        // 식물이 없거나 가족 구성원이 1명뿐일 경우 포인트 적립 중단
        if (pid == null) {
            throw new RuntimeException("새싹이 아직 생성되지 않아 포인트 적립이 불가능합니다.");
        }
        int memberCount = pointDao.getFamilyMemberCount(fid);
        if (memberCount < 2) {
            throw new IllegalStateException("가족 구성원이 2명 이상일 때만 포인트 적립이 가능합니다.");
        }

        //활동 타입에 해당하는 포인트 추출
        int point = activityPointMap.getOrDefault(type, 0);

        //활동 내역 저장 (Point_activities 테이블)
        Map<String, Object> activity = new HashMap<>();
        activity.put("uid", uid);
        activity.put("fid", fid);
        activity.put("pid", pid);
        activity.put("activity_type", type);
        activity.put("points_earned", point);
        activity.put("activity_date", LocalDate.now());
        activity.put("description", type + " 활동");

        pointDao.insertActivity(activity);

        //기존 경험치 조회 + 누적
        int currentExp = pointDao.getCurrentExperience(pid);
        int updatedExp = currentExp + point;
        pointDao.updateExperience(pid, updatedExp);

        // 현재 식물 레벨 및 가족 구성원 수 기반으로 레벨업 조건 계산
        int level = pointDao.getPlantLevel(pid);
        int required = getExpThreshold(pointDao.getFamilyMemberCount(fid), level);

        // 레벨업 조건 만족 시 → 레벨업 처리 및 경험치 초기화
        if (updatedExp >= required) {
            pointDao.levelUp(pid);
            pointDao.updateExperience(pid, 0); // 경험치 리셋
        }
    }

    private int getExpThreshold(int memberCount, int level) {
        if (memberCount < 2 || memberCount > 5 || level < 1 || level >= 5) {
            throw new IllegalArgumentException("지원하지 않는 상태입니다.");
        }
        int[][] table = {
                {},                      // index 0: 사용 안 함
                {},                      // index 1: 사용 안 함
                {0, 150, 200, 250, 300}, // 2인 가족
                {0, 200, 250, 300, 350}, // 3인 가족
                {0, 250, 300, 350, 400}, // 4인 가족
                {0, 300, 350, 400, 450}  // 5인 가족
        };
        return table[memberCount][level];
    }
}
