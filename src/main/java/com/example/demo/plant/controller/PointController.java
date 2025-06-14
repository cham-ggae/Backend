package com.example.demo.plant.controller;

import com.example.demo.login.service.AuthenticationService;
import com.example.demo.plant.dto.AddPointRequestDto;
import com.example.demo.plant.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;
    private final AuthenticationService authenticationService;

    @PostMapping("/add")
    public ResponseEntity<String> addPoint(@RequestBody AddPointRequestDto request) {
        Long uid = authenticationService.getCurrentUserId();
        pointService.addPoint(uid, request.getActivityType());
        return ResponseEntity.ok("포인트 적립 완료");
    }

    @GetMapping("/check-today/{type}")
    public ResponseEntity<Boolean> checkToday(@PathVariable String type) {
        Long uid = authenticationService.getCurrentUserId();
        boolean done = pointService.checkActivityExists(uid, type);
        return ResponseEntity.ok(done);
    }

    @GetMapping("/watered-members/{fid}")
    public ResponseEntity<List<Long>> getWateredMembers(@PathVariable Long fid) {
        List<Long> uids = pointService.getWateredMembers(fid);
        return ResponseEntity.ok(uids);
    }
}
