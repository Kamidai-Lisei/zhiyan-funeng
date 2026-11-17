package com.jay.aigcoj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.JsonParser;
import com.jay.aigcoj.bot.ds.DSApi;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 408 在线刷题：章节练习 / 历年真题卷 / 模拟卷
 * 选择题即时判分，大题由 AI 评阅并给出反馈
 */
@RestController
@RequestMapping("/quiz")
@Slf4j
public class QuizController {

    @Resource
    private QuizQuestionMapper quizQuestionMapper;

    @Resource
    private QuizRecordMapper quizRecordMapper;

    @Resource
    private UserService userService;

    @Resource
    private DSApi deepSeekApi;

    /**
     * 题目列表（不下发答案与解析，防止前端直接看到答案）
     */
    @GetMapping("/questions")
    public BaseResponse<List<Map<String, Object>>> questions(@RequestParam(required = false) String subject,
                                                             @RequestParam(required = false) String paper,
                                                             @RequestParam(required = false) Integer year,
                                                             @RequestParam(required = false) String type) {
        QueryWrapper<QuizQuestion> qw = new QueryWrapper<QuizQuestion>()
                .eq(StringUtils.isNotBlank(subject), "subject", subject)
                .eq(StringUtils.isNotBlank(paper), "paper", paper)
                .eq(year != null && year > 0, "year", year)
                .eq(StringUtils.isNotBlank(type), "type", type)
                .orderByAsc("id");
        List<QuizQuestion> list = quizQuestionMapper.selectList(qw);
        List<Map<String, Object>> result = list.stream().map(this::toPublic).collect(Collectors.toList());
        return ResultUtils.success(result);
    }

    /**
     * 卷子清单（真题卷 + 模拟卷 + 章节练习），含各卷题量
     */
    @GetMapping("/papers")
    public BaseResponse<List<Map<String, Object>>> papers() {
        List<QuizQuestion> all = quizQuestionMapper.selectList(null);
        Map<String, List<QuizQuestion>> byPaper = new LinkedHashMap<>();
        for (QuizQuestion q : all) {
            byPaper.computeIfAbsent(StringUtils.defaultIfBlank(q.getPaper(), "章节练习"),
                    k -> new ArrayList<>()).add(q);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<QuizQuestion>> e : byPaper.entrySet()) {
            List<QuizQuestion> qs = e.getValue();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", e.getKey());
            m.put("year", qs.get(0).getYear() == null ? 0 : qs.get(0).getYear());
            boolean realExam = e.getKey().contains("真题");
            m.put("kind", realExam ? "真题" : (e.getKey().contains("模拟") ? "模拟" : "章节"));
            m.put("choice", qs.stream().filter(q -> "choice".equals(q.getType())).count());
            m.put("subjective", qs.stream().filter(q -> "subjective".equals(q.getType())).count());
            m.put("total", qs.size());
            result.add(m);
        }
        // 章节练习排最后，真题按年份倒序
        result.sort((a, b) -> {
            int ka = "章节".equals(a.get("kind")) ? 1 : 0;
            int kb = "章节".equals(b.get("kind")) ? 1 : 0;
            if (ka != kb) return ka - kb;
            int ya = (int) a.get("year"), yb = (int) b.get("year");
            if (ya != yb) return yb - ya;
            return String.valueOf(a.get("name")).compareTo(String.valueOf(b.get("name")));
        });
        return ResultUtils.success(result);
    }

    /**
     * 科目概览
     */
    @GetMapping("/meta")
    public BaseResponse<Map<String, Object>> meta() {
        List<QuizQuestion> all = quizQuestionMapper.selectList(null);
        Map<String, Long> bySubject = all.stream()
                .collect(Collectors.groupingBy(QuizQuestion::getSubject, Collectors.counting()));
        Set<String> sources = all.stream().map(QuizQuestion::getPaper)
                .filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, Object> result = new HashMap<>();
        result.put("bySubject", bySubject);
        result.put("sources", sources);
        result.put("total", all.size());
        return ResultUtils.success(result);
    }

    /**
     * 单题作答：选择题自动判分，大题交由 AI 评阅，返回答案与解析
     */
    @PostMapping("/single")
    public BaseResponse<Map<String, Object>> single(@RequestBody Map<String, Object> body,
                                                    HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        long qid = Long.parseLong(String.valueOf(body.get("questionId")));
        QuizQuestion q = quizQuestionMapper.selectById(qid);
        ThrowUtils.throwIf(q == null, ErrorCode.NOT_FOUND_ERROR, "题目不存在");

        String type = StringUtils.defaultIfBlank(q.getType(), "choice");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("questionId", qid);
        result.put("type", type);
        result.put("answer", q.getAnswer());
        result.put("answerLong", q.getAnswerLong());
        result.put("analysis", q.getAnalysis());

        QuizRecord record = new QuizRecord();
        record.setUserId(user.getId());
        record.setQuestionId(qid);
        if ("subjective".equals(type)) {
            String answerText = StringUtils.trimToEmpty(String.valueOf(body.getOrDefault("answerText", "")));
            ThrowUtils.throwIf(StringUtils.isEmpty(answerText), ErrorCode.PARAMS_ERROR, "请先作答");
            record.setChosen(answerText);
            record.setCorrect(0);
            quizRecordMapper.insert(record);
            result.put("aiReviewed", true);
            result.put("aiComment", gradeSubjective(q, answerText));
        } else {
            String chosen = StringUtils.trimToEmpty(String.valueOf(body.getOrDefault("chosen", "")))
                    .toUpperCase();
            ThrowUtils.throwIf(StringUtils.isEmpty(chosen), ErrorCode.PARAMS_ERROR, "请先选择答案");
            boolean ok = StringUtils.equalsIgnoreCase(chosen, q.getAnswer());
            record.setChosen(chosen);
            record.setCorrect(ok ? 1 : 0);
            quizRecordMapper.insert(record);
            result.put("correct", ok);
            result.put("aiReviewed", false);
        }
        return ResultUtils.success(result);
    }

    /**
     * 整卷提交：逐题判分、写作答记录、返回答案与解析（选择题）
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
            if (q == null || "subjective".equals(q.getType())) {
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
        Map<String, int[]> bySubject = new LinkedHashMap<>();
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

    private Map<String, Object> toPublic(QuizQuestion q) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", q.getId());
        m.put("subject", q.getSubject());
        m.put("chapter", q.getChapter());
        m.put("paper", q.getPaper());
        m.put("year", q.getYear());
        m.put("type", StringUtils.defaultIfBlank(q.getType(), "choice"));
        m.put("question", q.getQuestion());
        try {
            List<String> opts = new ArrayList<>();
            JsonParser.parseString(q.getOptions()).getAsJsonArray().forEach(e -> opts.add(e.getAsString()));
            m.put("options", opts);
        } catch (Exception e) {
            m.put("options", new ArrayList<>());
        }
        return m;
    }

    /**
     * AI 评阅大题：对照参考答案给出得分、评语与改进建议
     */
    private String gradeSubjective(QuizQuestion q, String answerText) {
        String ref = StringUtils.defaultIfBlank(q.getAnswerLong(), q.getAnalysis());
        String prompt = "你是一名严格的 408（计算机学科专业基础综合）阅卷老师。\n" +
                "题目：" + q.getQuestion() + "\n" +
                "参考答案要点：" + StringUtils.defaultString(ref) + "\n" +
                "解析：" + StringUtils.defaultString(q.getAnalysis()) + "\n" +
                "学生作答：" + answerText + "\n\n" +
                "请按以下格式输出（总字数控制在 300 字内）：\n" +
                "得分：X/10\n" +
                "判定：正确 / 部分正确 / 错误\n" +
                "评语：<指出答对与答错的关键点>\n" +
                "建议：<1-2 条具体的改进建议>";
        try {
            String reply = deepSeekApi.sendDeepseekChat(
                    "你是严谨、友善的考研 408 阅卷与辅导专家，评语务实具体。", prompt);
            if (StringUtils.isNotBlank(reply)) {
                return reply;
            }
        } catch (Exception e) {
            log.warn("AI 评阅大题失败", e);
        }
        return "AI 评阅暂时不可用，可先对照参考答案要点自查：" + StringUtils.defaultString(ref);
    }
}
