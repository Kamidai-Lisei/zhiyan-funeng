package com.jay.aigcoj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jay.aigcoj.common.BaseResponse;
import com.jay.aigcoj.common.ErrorCode;
import com.jay.aigcoj.common.ResultUtils;
import com.jay.aigcoj.exception.ThrowUtils;
import com.jay.aigcoj.mapper.QuestionFavourMapper;
import com.jay.aigcoj.mapper.QuestionMapper;
import com.jay.aigcoj.model.entity.Question;
import com.jay.aigcoj.model.entity.QuestionFavour;
import com.jay.aigcoj.model.entity.User;
import com.jay.aigcoj.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题目收藏 / 错题本
 */
@RestController
@RequestMapping("/question_favour")
public class QuestionFavourController {

    @Resource
    private QuestionFavourMapper favourMapper;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private UserService userService;

    @PostMapping("/toggle")
    public BaseResponse<Map<String, Object>> toggle(@RequestBody Map<String, Long> body, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        Long questionId = body.get("questionId");
        ThrowUtils.throwIf(questionId == null, ErrorCode.PARAMS_ERROR);
        QuestionFavour existed = favourMapper.selectOne(new QueryWrapper<QuestionFavour>()
                .eq("questionId", questionId).eq("userId", user.getId()).last("limit 1"));
        boolean favour;
        if (existed != null) {
            favourMapper.deleteById(existed.getId());
            favour = false;
        } else {
            QuestionFavour record = new QuestionFavour();
            record.setQuestionId(questionId);
            record.setUserId(user.getId());
            favourMapper.insert(record);
            favour = true;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("favour", favour);
        return ResultUtils.success(result);
    }

    @GetMapping("/ids")
    public BaseResponse<List<Long>> ids(HttpServletRequest request) {
        User user = userService.getLoginUserPermitNull(request);
        if (user == null) {
            return ResultUtils.success(Collections.emptyList());
        }
        return ResultUtils.success(favourMapper.selectList(new QueryWrapper<QuestionFavour>()
                .eq("userId", user.getId())).stream().map(QuestionFavour::getQuestionId).collect(Collectors.toList()));
    }

    @GetMapping("/my")
    public BaseResponse<List<Question>> my(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        List<QuestionFavour> list = favourMapper.selectList(new QueryWrapper<QuestionFavour>()
                .eq("userId", user.getId()).orderByDesc("id"));
        if (list.isEmpty()) {
            return ResultUtils.success(Collections.emptyList());
        }
        List<Long> ids = list.stream().map(QuestionFavour::getQuestionId).collect(Collectors.toList());
        return ResultUtils.success(questionMapper.selectBatchIds(ids));
    }
}
