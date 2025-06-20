package com.example.demo.plant.service;

import com.example.demo.login.service.AuthenticationService;
import com.example.demo.plant.dao.PointDao;
import com.example.demo.plant.websocket.PlantWebSocketHandler;
import com.example.demo.plant.websocket.dto.PlantEventData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import static com.example.demo.plant.exception.PlantExceptions.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final PointDao pointDao;
    private final AuthenticationService authService;
    private final NutrientService nutrientService;

    // 활동 적용 시 자동완료를 위한 메서드
    public boolean checkActivityExists(Long uid, String type) {
        return pointDao.checkActivityExists(uid, type);
    }

    // 활동 유형별로 부여할 포인트 값을 매핑
    private final Map<String, Integer> activityPointMap = Map.of(
            "attendance", 5,
            "water", 300,
            "nutrient", 300,
            "emotion", 10,
            "quiz", 10,
            "lastleaf", 10,
            "register", 10,
            "survey", 5
    );

    //활동에 따른 포인트 적립 및 경험치 처리
    @Transactional
    public void addPoint(Long uid, String activityType) {
        // 영양제 사용시 영양제 1개 차감
        if (activityType.equals("nutrient")) {
            nutrientService.useNutrient(uid);
        }

        // 활동 1일 1회 제한
        if (pointDao.checkActivityExists(uid, activityType)) {
            throw new PointAlreadyAddedException("오늘 이미 이 활동을 완료했습니다.");
        }

        //사용자 ID → 가족 ID, 식물 ID 조회
        Long fid = pointDao.getFamilyIdByUid(uid);
        Long pid = pointDao.getPlantIdByFid(fid);

        // 식물이 없거나 가족 구성원이 1명뿐일 경우 포인트 적립 중단
        if (pid == null) {
            throw new PlantNotFoundException("새싹이 아직 생성되지 않아 포인트 적립이 불가능합니다.");
        }
        int memberCount = pointDao.getFamilyMemberCount(fid);
        if (memberCount < 2) {
            throw new NotEnoughFamilyMembersException("가족 구성원이 2명 이상일 때만 포인트 적립이 가능합니다.");
        }

        //활동 타입에 해당하는 포인트 추출
        int point = activityPointMap.getOrDefault(activityType, 0);

        //활동 내역 저장 (Point_activities 테이블)
        Map<String, Object> activity = new HashMap<>();
        activity.put("uid", uid);
        activity.put("fid", fid);
        activity.put("pid", pid);
        activity.put("activity_type", activityType);
        activity.put("points_earned", point);
        activity.put("activity_date", LocalDate.now());
        activity.put("description", activityType + " 활동");

        pointDao.insertActivity(activity);

        //기존 경험치 조회 + 누적
        int currentExp = pointDao.getCurrentExperience(pid);
        int updatedExp = currentExp + point;
        pointDao.updateExperience(pid, updatedExp);

        // 현재 식물 레벨 및 가족 구성원 수 기반으로 레벨업 조건 계산
        int level = pointDao.getPlantLevel(pid);
        int required = getExpThreshold(memberCount, level);
        boolean isLevelUp = false;

        // 레벨업 조건 만족 시 → 레벨업 처리 및 경험치 초기화
        if (updatedExp >= required) {
            pointDao.levelUp(pid);
            pointDao.updateExperience(pid, 0); // 경험치 리셋
            isLevelUp = true;
        }

        // ✅ 모든 활동에 대해 WebSocket 실시간 반영
        try {
            String name = pointDao.getUserName(uid);
            String avatarUrl = pointDao.getUserProfile(uid);

            PlantEventData event = new PlantEventData();
            event.setType(activityType);
            event.setFid(fid);
            event.setUid(uid);
            event.setName(name);
            event.setAvatarUrl(avatarUrl);
            event.setLevel(level + (isLevelUp ? 1 : 0));
            event.setExperiencePoint(isLevelUp ? 0 : updatedExp);
            event.setExpThreshold(required);
            event.setLevelUp(isLevelUp);

            String json = new ObjectMapper().writeValueAsString(event);
            for (WebSocketSession s : PlantWebSocketHandler.sessions) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(json));
                }
            }

            // ✅ water일 경우 영양제 추가 확인
            if (activityType.equals("water")) {
                Date today = Date.valueOf(LocalDate.now());
                int watered = pointDao.countWateredMembersToday(fid, today);
                if (memberCount == watered) {
                    pointDao.incrementNutrient(fid);
                }
            }

        } catch (Exception e) {
            log.warn("WebSocket 브로드캐스트 에러: {}", e.getMessage());
        }
    }

    public int getExpThreshold(int memberCount, int level) {
        if (memberCount < 2 || memberCount > 5 || level < 1 || level >= 5) {
            throw new IllegalArgumentException("지원하지 않는 상태입니다.");
        }
        int[][] table = {
                {},
                {},
                {0, 150, 200, 250, 300},
                {0, 200, 250, 300, 350},
                {0, 250, 300, 350, 400},
                {0, 300, 350, 400, 450}
        };
        return table[memberCount][level];
    }

    // 오늘 기준으로 해당 가족(fid)에서 'water' 활동을 한 uid 목록 반환
    public List<Long> getWateredMembers(Long fid) {
        Date today = Date.valueOf(LocalDate.now());
        return pointDao.getTodayWateredUids(fid, today);
    }

    public int getPlantLevel(Long pid) {
        return pointDao.getPlantLevel(pid);
    }
}
