package com.jay.aigcoj.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.jay.aigcoj.mapper.*;
import com.jay.aigcoj.model.entity.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 个人考研画像：聚合刷题、错题、打卡等行为数据，是"一人一档"的数据底座
 */
@Service
public class PortraitService {

    @Resource
    private QuestionSubmitMapper submitMapper;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private StudyCheckinMapper checkinMapper;

    @Resource
    private StudyPlanMapper planMapper;

    @Resource
    private QuestionFavourMapper questionFavourMapper;

    /**
     * 构建结构化画像数据
     */
    public Map<String, Object> buildPortrait(Long userId) {
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 刷题总览
        List<QuestionSubmit> submits = submitMapper.selectList(new QueryWrapper<QuestionSubmit>()
                .eq("userId", userId).eq("status", 2).orderByDesc("id"));
        long accepted = submits.stream().filter(s -> isAccepted(s.getJudgeInfo())).count();
        result.put("submitCount", submits.size());
        result.put("acceptedCount", accepted);
        result.put("acceptance", submits.isEmpty() ? 0 : Math.round(accepted * 100.0 / submits.size()));

        // 2. 按知识标签聚合掌握度
        Set<Long> qids = submits.stream().map(QuestionSubmit::getQuestionId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Question> questionMap = qids.isEmpty() ? Collections.emptyMap()
                : questionMapper.selectBatchIds(qids).stream()
                .collect(Collectors.toMap(Question::getId, q -> q, (a, b) -> a));
        Map<String, int[]> tagAgg = new LinkedHashMap<>(); // tag -> [total, accepted]
        for (QuestionSubmit s : submits) {
            Question q = questionMap.get(s.getQuestionId());
            if (q == null) {
                continue;
            }
            for (String tag : parseTags(q.getTags())) {
                int[] agg = tagAgg.computeIfAbsent(tag, k -> new int[2]);
                agg[0]++;
                if (isAccepted(s.getJudgeInfo())) {
                    agg[1]++;
                }
            }
        }
        List<Map<String, Object>> tagStats = tagAgg.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<String, Object>();
                    m.put("tag", e.getKey());
                    m.put("total", e.getValue()[0]);
                    m.put("accepted", e.getValue()[1]);
                    // Math.round 返回 long，强转 int，避免下游 (Integer) 强转抛 ClassCastException
                    m.put("mastery", e.getValue()[0] == 0 ? 0 : (int) Math.round(e.getValue()[1] * 100.0 / e.getValue()[0]));
                    return m;
                })
                .sorted((a, b) -> Integer.compare((Integer) b.get("total"), (Integer) a.get("total")))
                .collect(Collectors.toList());
        result.put("tagStats", tagStats.stream().limit(12).collect(Collectors.toList()));
        // 薄弱点：做过但正确率低的标签；擅长：正确率高的标签
        List<String> weakTags = tagStats.stream().filter(t -> (Integer) t.get("total") >= 1 && (Integer) t.get("mastery") < 60)
                .sorted(Comparator.comparingInt(t -> (Integer) t.get("mastery")))
                .map(t -> (String) t.get("tag")).limit(5).collect(Collectors.toList());
        List<String> strongTags = tagStats.stream().filter(t -> (Integer) t.get("mastery") >= 80)
                .map(t -> (String) t.get("tag")).limit(5).collect(Collectors.toList());
        result.put("weakTags", weakTags);
        result.put("strongTags", strongTags);

        // 3. 最近错题（去重取前 6 道）
        List<Map<String, Object>> recentWrong = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (QuestionSubmit s : submits) {
            if (isAccepted(s.getJudgeInfo()) || !questionMap.containsKey(s.getQuestionId()) || seen.contains(s.getQuestionId())) {
                continue;
            }
            seen.add(s.getQuestionId());
            Question q = questionMap.get(s.getQuestionId());
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("id", String.valueOf(q.getId()));
            m.put("title", q.getTitle());
            recentWrong.add(m);
            if (recentWrong.size() >= 6) {
                break;
            }
        }
        result.put("recentWrong", recentWrong);

        // 4. 学习投入
        List<StudyCheckin> checkins = checkinMapper.selectList(new QueryWrapper<StudyCheckin>()
                .eq("userId", userId).orderByDesc("checkinDate"));
        int totalMinutes = checkins.stream().mapToInt(c -> c.getDuration() == null ? 0 : c.getDuration()).sum();
        LocalDate twoWeeksAgo = LocalDate.now().minusDays(13);
        long activeDays14 = checkins.stream()
                // DATE 列实际映射为 java.sql.Date：toInstant() 会抛异常、声明类型 java.util.Date 又没有 toLocalDate()，统一经 Timestamp 转换
                .map(c -> new java.sql.Timestamp(c.getCheckinDate().getTime()).toLocalDateTime().toLocalDate())
                .filter(d -> !d.isBefore(twoWeeksAgo)).distinct().count();
        result.put("checkinDays", checkins.size());
        result.put("totalMinutes", totalMinutes);
        result.put("activeDays14", activeDays14);

        // 5. 收藏与计划
        result.put("favourCount", questionFavourMapper.selectCount(
                new QueryWrapper<QuestionFavour>().eq("userId", userId)));
        StudyPlan plan = planMapper.selectOne(new QueryWrapper<StudyPlan>().eq("userId", userId).last("limit 1"));
        if (plan != null) {
            Map<String, Object> planInfo = new HashMap<String, Object>();
            planInfo.put("targetDate", plan.getTargetDate() == null ? ""
                    : new java.sql.Timestamp(plan.getTargetDate().getTime()).toLocalDateTime().toLocalDate().toString());
            planInfo.put("dailyMinutes", plan.getDailyMinutes());
            planInfo.put("subjects", plan.getSubjects());
            result.put("plan", planInfo);
        }
        return result;
    }

    /**
     * 画像压缩成一段给大模型看的文字
     */
    @SuppressWarnings("unchecked")
    public String portraitText(Long userId) {
        Map<String, Object> p = buildPortrait(userId);
        StringBuilder sb = new StringBuilder();
        sb.append("累计提交 ").append(p.get("submitCount")).append(" 次，通过 ").append(p.get("acceptedCount"))
                .append(" 次，整体正确率 ").append(p.get("acceptance")).append("%；");
        sb.append("累计打卡 ").append(p.get("checkinDays")).append(" 天、投入 ").append(p.get("totalMinutes"))
                .append(" 分钟，近两周活跃 ").append(p.get("activeDays14")).append(" 天；");
        List<String> weak = (List<String>) p.get("weakTags");
        List<String> strong = (List<String>) p.get("strongTags");
        sb.append("薄弱知识点：").append(weak.isEmpty() ? "暂不明显" : String.join("、", weak)).append("；");
        sb.append("掌握较好：").append(strong.isEmpty() ? "暂不明显" : String.join("、", strong)).append("。");
        Object plan = p.get("plan");
        if (plan instanceof Map) {
            Map<String, Object> pl = (Map<String, Object>) plan;
            sb.append("目标考试日期 ").append(pl.get("targetDate")).append("，计划每日学习 ")
                    .append(pl.get("dailyMinutes")).append(" 分钟。");
        }
        return sb.toString();
    }

    private boolean isAccepted(String judgeInfo) {
        return judgeInfo != null && judgeInfo.contains("Accepted");
    }

    private List<String> parseTags(String tags) {
        if (tags == null || tags.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            JsonArray arr = JsonParser.parseString(tags).getAsJsonArray();
            List<String> list = new ArrayList<>();
            arr.forEach(e -> list.add(e.getAsString()));
            return list;
        } catch (Exception e) {
            return Arrays.stream(tags.split("[,，]")).map(String::trim)
                    .filter(s -> !s.isEmpty()).collect(Collectors.toList());
        }
    }
}
