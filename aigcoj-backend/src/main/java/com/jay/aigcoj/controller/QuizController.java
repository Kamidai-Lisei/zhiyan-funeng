package com.jay.aigcoj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.JsonParser;
import com.jay.aigcoj.common.BaseResponse;
import com.jay.aigcoj.common.ErrorCode;
import com.jay.aigcoj.common.ResultUtils;
import com.jay.aigcoj.exception.ThrowUtils;
import com.jay.aigcoj.mapper.QuizQuestionMapper;
import com.jay.aigcoj.mapper.QuizRecordMapper;
import com.jay.aigcoj.model.entity.QuizQuestion;
import com.jay.aigcoj.model.entity.QuizRecord;
import com.jay.aigcoj.model.entity.User;
import com.jay.aigcoj.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 408 在线习题：四科章节练习 + 往年真题，在线作答即时判分
 */
@RestController
@RequestMapping("/quiz")
public class QuizController {

    @Resource
    private QuizQuestionMapper quizQuestionMapper;

    @Resource
    private QuizRecordMapper quizRecordMapper;

    @Resource
    private UserService userService;

    /**
     * 题目列表（不下发答案与解析，防止前端直接看到答案）
     */
    @GetMapping("/questions")
    public BaseResponse<List<Map<String, Object>>> questions(@RequestParam(required = false) String subject,
                                                             @RequestParam(required = false) String source) {
        List<QuizQuestion> list = quizQuestionMapper.selectList(new QueryWrapper<QuizQuestion>()
                .eq(StringUtils.isNotBlank(subject), "subject", subject)
                .eq(StringUtils.isNotBlank(source), "source", source)
                .orderByAsc("id"));
        List<Map<String, Object>> result = list.stream().map(q -> {
            Map<String, Object> m = new LinkedHashMap<String, Object>();
            m.put("id", q.getId());
            m.put("subject", q.getSubject());
            m.put("chapter", q.getChapter());
            m.put("source", q.getSource());
            m.put("question", q.getQuestion());
            try {
                // 注意：必须用 Java List，Gson 的 JsonArray 交给 Jackson 序列化会变成 {"empty":false}
                List<String> opts = new ArrayList<>();
                JsonParser.parseString(q.getOptions()).getAsJsonArray().forEach(e -> opts.add(e.getAsString()));
                m.put("options", opts);
            } catch (Exception e) {
                m.put("options", new ArrayList<>());
            }
            return m;
        }).collect(Collectors.toList());
        return ResultUtils.success(result);
    }

    /**
     * 来源与科目概览（含各科目题量）
     */
    @GetMapping("/meta")
    public BaseResponse<Map<String, Object>> meta() {
        List<QuizQuestion> all = quizQuestionMapper.selectList(null);
        Map<String, Long> bySubject = all.stream()
                .collect(Collectors.groupingBy(QuizQuestion::getSubject, Collectors.counting()));
        Set<String> sources = all.stream().map(QuizQuestion::getSource)
                .filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, Object> result = new HashMap<>();
        result.put("bySubject", bySubject);
        result.put("sources", sources);
        result.put("total", all.size());
        return ResultUtils.success(result);
    }

    /**
     * 提交作答：逐题判分、写作答记录、返回答案与解析
     */
    @PostMapping("/submit")
    public BaseResponse<Map<String, Object>> submit(@RequestBody Map<String, Object> body,
                                                    HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        Object raw = body.get("answers");
        ThrowUtils.throwIf(!(raw instanceof List) || ((List<?>) raw).isEmpty(),
                ErrorCode.PARAMS_ERROR, "请先作答再提交");
        List<?> answers = (List<?>) raw;

        Set<Long> ids = new HashSet<>();
        Map<Long, String> chosenMap = new LinkedHashMap<>();
        for (Object o : answers) {
            if (o instanceof Map) {
                Long qid = Long.valueOf(String.valueOf(((Map<?, ?>) o).get("questionId")));
                String chosen = StringUtils.trimToEmpty(String.valueOf(((Map<?, ?>) o).get("chosen")));
                if (!chosen.isEmpty()) {
                    ids.add(qid);
                    chosenMap.put(qid, chosen.toUpperCase());
                }
            }
        }
        Map<Long, QuizQuestion> questionMap = ids.isEmpty() ? Collections.emptyMap()
                : quizQuestionMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(QuizQuestion::getId, q -> q));

        int correct = 0;
        List<Map<String, Object>> results = new ArrayList<>();
        for (Map.Entry<Long, String> entry : chosenMap.entrySet()) {
            QuizQuestion q = questionMap.get(entry.getKey());
            if (q == null) {
                continue;
            }
            boolean ok = StringUtils.equalsIgnoreCase(entry.getValue(), q.getAnswer());
            if (ok) {
                correct++;
            }
            QuizRecord record = new QuizRecord();
            record.setUserId(user.getId());
            record.setQuestionId(q.getId());
            record.setChosen(entry.getValue());
            record.setCorrect(ok ? 1 : 0);
            quizRecordMapper.insert(record);

            Map<String, Object> r = new LinkedHashMap<String, Object>();
            r.put("questionId", q.getId());
            r.put("chosen", entry.getValue());
            r.put("answer", q.getAnswer());
            r.put("correct", ok);
            r.put("analysis", q.getAnalysis());
            results.add(r);
        }
        int total = results.size();
        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("correct", correct);
        result.put("score", total == 0 ? 0 : Math.round(correct * 100f / total));
        result.put("results", results);
        return ResultUtils.success(result);
    }

    /**
     * 我的习题正确率（按科目）
     */
    @GetMapping("/stats")
    public BaseResponse<Map<String, Object>> stats(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        List<QuizRecord> records = quizRecordMapper.selectList(new QueryWrapper<QuizRecord>()
                .eq("userId", user.getId()));
        Set<Long> qids = records.stream().map(QuizRecord::getQuestionId).collect(Collectors.toSet());
        Map<Long, QuizQuestion> questionMap = qids.isEmpty() ? Collections.emptyMap()
                : quizQuestionMapper.selectBatchIds(qids).stream()
                .collect(Collectors.toMap(QuizQuestion::getId, q -> q));
        Map<String, int[]> bySubject = new LinkedHashMap<>(); // subject -> [total, correct]
        for (QuizRecord r : records) {
            QuizQuestion q = questionMap.get(r.getQuestionId());
            if (q == null) {
                continue;
            }
            int[] agg = bySubject.computeIfAbsent(q.getSubject(), k -> new int[2]);
            agg[0]++;
            if (r.getCorrect() != null && r.getCorrect() == 1) {
                agg[1]++;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("answered", records.size());
        result.put("bySubject", bySubject.entrySet().stream().map(e -> {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("subject", e.getKey());
            m.put("total", e.getValue()[0]);
            m.put("correct", e.getValue()[1]);
            m.put("accuracy", e.getValue()[0] == 0 ? 0 : Math.round(e.getValue()[1] * 100f / e.getValue()[0]));
            return m;
        }).collect(Collectors.toList()));
        return ResultUtils.success(result);
    }
}
