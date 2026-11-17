package com.jay.aigcoj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jay.aigcoj.common.BaseResponse;
import com.jay.aigcoj.common.ErrorCode;
import com.jay.aigcoj.common.ResultUtils;
import com.jay.aigcoj.exception.ThrowUtils;
import com.jay.aigcoj.mapper.*;
import com.jay.aigcoj.model.entity.*;
import com.jay.aigcoj.service.PortraitService;
import com.jay.aigcoj.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学习中心：打卡、进度、计划、错题本
 */
@RestController
@RequestMapping("/study")
public class StudyController {

    @Resource
    private StudyCheckinMapper checkinMapper;

    @Resource
    private StudyProgressMapper progressMapper;

    @Resource
    private StudyPlanMapper planMapper;

    @Resource
    private QuestionSubmitMapper submitMapper;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private QuestionFavourMapper questionFavourMapper;

    @Resource
    private UserService userService;

    @Resource
    private PortraitService portraitService;

    /**
     * 个人考研画像（一人一档数据底座）
     */
    @GetMapping("/portrait")
    public BaseResponse<Map<String, Object>> portrait(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(portraitService.buildPortrait(user.getId()));
    }

    @PostMapping("/checkin")
    public BaseResponse<Boolean> checkin(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        int duration = body.get("duration") == null ? 60 : Integer.parseInt(String.valueOf(body.get("duration")));
        String content = body.get("content") == null ? "" : String.valueOf(body.get("content"));
        Date today = Date.valueOf(LocalDate.now());
        StudyCheckin existed = checkinMapper.selectOne(new QueryWrapper<StudyCheckin>()
                .eq("userId", user.getId()).eq("checkinDate", today).last("limit 1"));
        if (existed != null) {
            existed.setDuration(duration);
            existed.setContent(content);
            checkinMapper.updateById(existed);
        } else {
            StudyCheckin record = new StudyCheckin();
            record.setUserId(user.getId());
            record.setCheckinDate(today);
            record.setDuration(duration);
            record.setContent(content);
            checkinMapper.insert(record);
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/checkin/today")
    public BaseResponse<StudyCheckin> todayCheckin(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(checkinMapper.selectOne(new QueryWrapper<StudyCheckin>()
                .eq("userId", user.getId()).eq("checkinDate", Date.valueOf(LocalDate.now())).last("limit 1")));
    }

    @GetMapping("/checkin/calendar")
    public BaseResponse<List<String>> calendar(@RequestParam(required = false) String month,
                                               HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        if (month != null && month.length() >= 7) {
            start = LocalDate.parse(month.substring(0, 7) + "-01");
        }
        LocalDate end = start.plusMonths(1).minusDays(1);
        List<StudyCheckin> list = checkinMapper.selectList(new QueryWrapper<StudyCheckin>()
                .eq("userId", user.getId())
                .between("checkinDate", Date.valueOf(start), Date.valueOf(end))
                .orderByAsc("checkinDate"));
        List<String> dates = list.stream().map(i -> toLocalDate(i.getCheckinDate()).toString()).collect(Collectors.toList());
        return ResultUtils.success(dates);
    }

    @GetMapping("/progress")
    public BaseResponse<List<StudyProgress>> progress(@RequestParam(required = false) String module,
                                                      HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(progressMapper.selectList(new QueryWrapper<StudyProgress>()
                .eq("userId", user.getId())
                .eq(module != null, "module", module)));
    }

    @PostMapping("/progress")
    public BaseResponse<Boolean> saveProgress(@RequestBody Map<String, String> body, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        String module = body.get("module");
        String slug = body.get("slug");
        ThrowUtils.throwIf(module == null || slug == null, ErrorCode.PARAMS_ERROR);
        boolean finished = !"false".equals(String.valueOf(body.get("finished")));
        StudyProgress existed = progressMapper.selectOne(new QueryWrapper<StudyProgress>()
                .eq("userId", user.getId()).eq("module", module).eq("slug", slug).last("limit 1"));
        if (existed == null) {
            StudyProgress record = new StudyProgress();
            record.setUserId(user.getId());
            record.setModule(module);
            record.setSlug(slug);
            record.setFinished(finished ? 1 : 0);
            progressMapper.insert(record);
        } else {
            existed.setFinished(finished ? 1 : 0);
            progressMapper.updateById(existed);
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/plan")
    public BaseResponse<StudyPlan> plan(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        StudyPlan plan = planMapper.selectOne(new QueryWrapper<StudyPlan>()
                .eq("userId", user.getId()).last("limit 1"));
        if (plan == null) {
            plan = new StudyPlan();
            plan.setUserId(user.getId());
            plan.setTargetDate(Date.valueOf(LocalDate.of(2026, 12, 19)));
            plan.setDailyMinutes(180);
            plan.setSubjects("数据结构,计算机组成原理,操作系统,计算机网络");
            plan.setMotto("把每一天的复习，变成上岸的确定性。");
            planMapper.insert(plan);
        }
        return ResultUtils.success(plan);
    }

    @PostMapping("/plan")
    public BaseResponse<Boolean> savePlan(@RequestBody StudyPlan body, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        StudyPlan plan = planMapper.selectOne(new QueryWrapper<StudyPlan>()
                .eq("userId", user.getId()).last("limit 1"));
        if (plan == null) {
            body.setUserId(user.getId());
            planMapper.insert(body);
        } else {
            body.setId(plan.getId());
            body.setUserId(user.getId());
            planMapper.updateById(body);
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/wrong-book")
    public BaseResponse<List<Question>> wrongBook(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        QueryWrapper<QuestionSubmit> qw = new QueryWrapper<>();
        qw.eq("userId", user.getId()).eq("status", 2)
                .and(w -> w.like("judgeInfo", "Wrong Answer")
                        .or().like("judgeInfo", "Compile Error")
                        .or().like("judgeInfo", "Time Limit Exceeded")
                        .or().like("judgeInfo", "Runtime Error"))
                .orderByDesc("id");
        List<QuestionSubmit> submits = submitMapper.selectList(qw);
        Set<Long> questionIds = submits.stream().map(QuestionSubmit::getQuestionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (questionIds.isEmpty()) {
            return ResultUtils.success(Collections.emptyList());
        }
        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        Map<Long, Question> map = questions.stream().collect(Collectors.toMap(Question::getId, q -> q));
        List<Question> ordered = questionIds.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
        return ResultUtils.success(ordered);
    }

    @GetMapping("/overview")
    public BaseResponse<Map<String, Object>> overview(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        Map<String, Object> result = new HashMap<>();
        long checkinDays = checkinMapper.selectCount(new QueryWrapper<StudyCheckin>().eq("userId", user.getId()));
        List<StudyCheckin> all = checkinMapper.selectList(new QueryWrapper<StudyCheckin>()
                .eq("userId", user.getId()).orderByDesc("checkinDate"));
        int totalMinutes = all.stream().mapToInt(i -> i.getDuration() == null ? 0 : i.getDuration()).sum();
        int streak = 0;
        LocalDate cursor = LocalDate.now();
        Set<LocalDate> dates = all.stream().map(i -> toLocalDate(i.getCheckinDate())).collect(Collectors.toSet());
        if (!dates.contains(cursor)) {
            cursor = cursor.minusDays(1);
        }
        while (dates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        long submitCount = submitMapper.selectCount(new QueryWrapper<QuestionSubmit>().eq("userId", user.getId()));
        long acceptedCount = submitMapper.selectCount(new QueryWrapper<QuestionSubmit>()
                .eq("userId", user.getId()).eq("status", 2).like("judgeInfo", "Accepted"));
        long wrongCount = submitMapper.selectCount(new QueryWrapper<QuestionSubmit>()
                .eq("userId", user.getId()).eq("status", 2).like("judgeInfo", "Wrong Answer"));
        long favourCount = questionFavourMapper.selectCount(new QueryWrapper<QuestionFavour>()
                .eq("userId", user.getId()));
        result.put("checkinDays", checkinDays);
        result.put("totalMinutes", totalMinutes);
        result.put("streak", streak);
        result.put("submitCount", submitCount);
        result.put("acceptedCount", acceptedCount);
        result.put("wrongCount", wrongCount);
        result.put("favourCount", favourCount);
        result.put("recentCheckins", all.stream().limit(30).map(i -> toLocalDate(i.getCheckinDate()).toString()).collect(Collectors.toList()));
        return ResultUtils.success(result);
    }

    private LocalDate toLocalDate(java.util.Date date) {
        // DATE 列实际是 java.sql.Date：toInstant() 会抛异常，统一经 Timestamp 转换为 LocalDate
        return new java.sql.Timestamp(date.getTime()).toLocalDateTime().toLocalDate();
    }
}
