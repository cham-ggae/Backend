package com.example.demo.mypage.service;

import com.example.demo.login.service.AuthenticationService;
import com.example.demo.mypage.dto.MyPageResponse;
import com.example.demo.mypage.dto.RecommendHistoryData;
import com.example.demo.mypage.mapper.BugMapper;
import com.example.demo.mypage.mapper.PlanMapper;
import com.example.demo.mypage.mapper.UserMapper;
import com.example.demo.surveyResult.dto.SurveyResponseDto;
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

    /**
     * getMyPageInfo 메서드입니다.
     * @return 반환값 설명
     */
    public MyPageResponse getMyPageInfo() throws NotFoundException {
        Long uid = authenticationService.getCurrentUserId();
        if (uid==null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        MyPageResponse.UserInfo userInfo = userMapper.findUserInfoById(uid);
        if (userInfo == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"사용자를 찾을 수 없습니다.");
        };
        MyPageResponse response = new MyPageResponse();
        response.setUserInfo(userInfo);

        if (userInfo.getBugId() != null) {
            SurveyResponseDto bugInfo = bugMapper.findBugInfoById(userInfo.getBugId());

            // 설문 결과 세팅
            MyPageResponse.SurveyResult surveyResult = new MyPageResponse.SurveyResult();
            surveyResult.setBugName(bugInfo.getBugName());
            surveyResult.setFeature(bugInfo.getFeature());
            surveyResult.setPersonality(bugInfo.getPersonality());
            response.setSurveyResult(surveyResult);

            List<MyPageResponse.RecommendHistory> historyList = new ArrayList<>();

            RecommendHistoryData data1 = planMapper.findPlanById(bugInfo.getSuggest1());
            System.out.println(">>> suggest1: " + bugInfo.getSuggest1());
            System.out.println(">>> data1: " + data1);
            RecommendHistoryData data2 = planMapper.findPlanById(bugInfo.getSuggest2());
            System.out.println(">>> suggest2: " + bugInfo.getSuggest2());
            System.out.println(">>> data2: " + data2);

            if (data1 != null) {
                MyPageResponse.RecommendHistory rh1 = new MyPageResponse.RecommendHistory();
                rh1.setPlanId(data1.getPlanId());
                rh1.setPlanName(data1.getPlanName());
                rh1.setPrice(data1.getPrice());
                rh1.setDiscountPrice(data1.getDiscountPrice());
                rh1.setBenefit(data1.getBenefit());
                rh1.setLink(data1.getLink());
                historyList.add(rh1);
            }

            if (data2 != null) {
                MyPageResponse.RecommendHistory rh2 = new MyPageResponse.RecommendHistory();
                rh2.setPlanId(data2.getPlanId());
                rh2.setPlanName(data2.getPlanName());
                rh2.setPrice(data2.getPrice());
                rh2.setDiscountPrice(data2.getDiscountPrice());
                rh2.setBenefit(data2.getBenefit());
                rh2.setLink(data2.getLink());
                historyList.add(rh2);
            }

            response.setRecommendHistory(historyList);
        }

        return response;

    }

}
