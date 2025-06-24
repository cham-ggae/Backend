package com.example.demo.mypage.service;

import com.example.demo.login.service.AuthenticationService;
import com.example.demo.mypage.dto.MyPageResponse;
import com.example.demo.mypage.dto.RecommendHistoryData;
import com.example.demo.mypage.mapper.BugMapper;
import com.example.demo.mypage.mapper.PlanMapper;
import com.example.demo.mypage.mapper.UserMapper;
import com.example.demo.surveyResult.dto.SurveyResponseDto;
import com.example.demo.surveyResult.mapper.MypageMapper;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.jmx.export.UnableToRegisterMBeanException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
/**
 * MyPageService 클래스입니다.
 */
public class MyPageService {
    private final UserMapper userMapper;
    private final BugMapper bugMapper;
    private final PlanMapper planMapper;
    private final AuthenticationService authenticationService;
    private final MypageMapper myPageMapper;
    /**
     * 히스토리 전용: 과거 추천받은 요금제 쌍들을 반환합니다.
     */

    public List<RecommendHistoryData> getRecommendHistory(Long userId) {
        return myPageMapper.findRecommendHistory(userId);
    }
        /**
         * 마이페이지 전체 정보 조회 (기본정보 + 현재 설문 결과 + 추천 요금제 2개)
         */
    public MyPageResponse getMyPageInfo() {
            Long uid = authenticationService.getCurrentUserId();
            if (uid == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
            }
            MyPageResponse.UserInfo userInfo = userMapper.findUserInfoById(uid);
            if (userInfo == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
            }

            MyPageResponse response = new MyPageResponse();
            response.setUserInfo(userInfo);

            //설문조사 결과가 있는 경우만 조회
            if (userInfo.getBugId() != null) {
                SurveyResponseDto bugInfo = bugMapper.findBugInfoById(userInfo.getBugId());

                // 설문 결과 세팅
                MyPageResponse.SurveyResult surveyResult = new MyPageResponse.SurveyResult();
                surveyResult.setBugName(bugInfo.getBugName());
                surveyResult.setFeature(bugInfo.getFeature());
                surveyResult.setPersonality(bugInfo.getPersonality());
                response.setSurveyResult(surveyResult);

                RecommendHistoryData data1 = planMapper.findPlanById(bugInfo.getSuggest1());
                RecommendHistoryData data2 = planMapper.findPlanById(bugInfo.getSuggest2());
                List<MyPageResponse.RecommendHistory> historyList = new ArrayList<>();

                if(data1 !=null) historyList.add(convertToHistory(data1));
                if(data2 !=null) historyList.add(convertToHistory(data2));


                response.setRecommendHistory(historyList);
            }
            return response;
        }
    /**
     * 요금제 DTO → RecommendHistory 변환 메서드
     */
    private MyPageResponse.RecommendHistory convertToHistory(RecommendHistoryData data) {
        MyPageResponse.RecommendHistory rh = new MyPageResponse.RecommendHistory();
        rh.setPlanId(data.getPlanId());
        rh.setPlanName(data.getPlanName());
        rh.setPrice(data.getPrice());
        rh.setDiscountPrice(data.getDiscountPrice());
        rh.setBenefit(data.getBenefit());
        rh.setLink(data.getLink());
        return rh;
    }
    }
