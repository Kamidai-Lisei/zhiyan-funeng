package com.jay.aigcoj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jay.aigcoj.bot.ds.DSApi;
import com.jay.aigcoj.common.BaseResponse;
import com.jay.aigcoj.common.ErrorCode;
import com.jay.aigcoj.common.ResultUtils;
import com.jay.aigcoj.exception.ThrowUtils;
import com.jay.aigcoj.mapper.AiMemoryMapper;
import com.jay.aigcoj.model.entity.AiMemory;
import com.jay.aigcoj.model.entity.User;
import com.jay.aigcoj.service.PortraitService;
import com.jay.aigcoj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智研赋能 · AI 能力中心：智能问答、择校决策推演、个性化学习规划、AI 长期记忆
 */
@RestController
@RequestMapping("/ai")
@Slf4j
public class AiController {

    @Resource
    private DSApi deepSeekApi;

    @Resource
    private AiMemoryMapper aiMemoryMapper;

    @Resource
    private PortraitService portraitService;

    @Resource
    private UserService userService;

    private static final Map<String, String> PROMPTS = new HashMap<>();

    static {
        PROMPTS.put("study", "你是计算机考研 408 助教。用循序渐进的方式讲解知识点，适当举例，并在最后给出 1 道自测题。");
        PROMPTS.put("code", "你是算法竞赛教练。分析代码的时间复杂度、边界情况与优化方向，给出可运行的修改建议。");
        PROMPTS.put("interview", "你是互联网大厂技术面试官。围绕计算机基础、项目与算法提问，并给出评估与改进建议。");
        PROMPTS.put("resume", "你是计算机考研复试与求职简历顾问。给出具体、量化、可执行的修改建议。");
        PROMPTS.put("plan", "你是考研规划师。根据目标日期与基础，给出阶段化、可执行的复习计划。");
    }

    @PostMapping("/assistant")
    public BaseResponse<Map<String, Object>> assistant(@RequestBody Map<String, String> body) {
        String message = StringUtils.trimToEmpty(body.get("message"));
        ThrowUtils.throwIf(message.isEmpty(), ErrorCode.PARAMS_ERROR, "请输入问题");
        String scene = StringUtils.defaultIfBlank(body.get("scene"), "study");
        String context = StringUtils.defaultString(body.get("context"));
        String system = PROMPTS.getOrDefault(scene, PROMPTS.get("study"));
        String prompt = message;
        if (StringUtils.isNotBlank(context)) {
            prompt = "【当前页面上下文】\n" + context + "\n\n【我的问题】\n" + message;
        }
        String answer;
        try {
            answer = deepSeekApi.sendDeepseekChat(system, prompt);
        } catch (Exception e) {
            log.warn("AI 助手调用失败", e);
            answer = "AI 服务暂时不可用。你可以先复习 408 的核心知识框架：数据结构（线性表、树、图、排序）、"
                    + "计算机组成原理（数据表示、存储层次、指令系统）、操作系统（进程、内存、文件）、"
                    + "计算机网络（分层模型、TCP/IP、HTTP）。稍后重试即可获得更详细的讲解。";
        }
        Map<String, Object> result = new HashMap<>();
        result.put("answer", answer);
        result.put("scene", scene);
        return ResultUtils.success(result);
    }

    // ================= AI 学习决策模拟器 =================

    /**
     * 择校/备考决策推演：冲稳保多情景对比，量化上岸概率
     */
    @PostMapping("/simulate")
    public BaseResponse<Map<String, Object>> simulate(@RequestBody Map<String, Object> body,
                                                      HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        String school = StringUtils.trimToEmpty(String.valueOf(body.getOrDefault("school", "")));
        ThrowUtils.throwIf(school.isEmpty(), ErrorCode.PARAMS_ERROR, "请填写目标院校");
        String major = StringUtils.defaultIfBlank(String.valueOf(body.getOrDefault("major", "")), "计算机科学与技术");
        int currentScore = parseInt(body.get("currentScore"), 280);
        int targetScore = parseInt(body.get("targetScore"), 340);
        int dailyHours = parseInt(body.get("dailyHours"), 6);
        String stage = StringUtils.defaultIfBlank(String.valueOf(body.getOrDefault("stage", "")), "强化阶段");
        String weakSubjects = StringUtils.defaultIfBlank(String.valueOf(body.getOrDefault("weakSubjects", "")), "暂不明确");

        // 个人画像是推演的输入之一 —— 这是"认识用户"的关键
        String portrait = portraitService.portraitText(user.getId());

        Map<String, Object> scenario = buildScenarios(currentScore, targetScore, dailyHours);

        String system = "你是考研决策推演引擎。基于用户画像与目标信息，做多情景量化对比分析，"
                + "输出 markdown 报告，包含：1) 差距诊断；2) 冲刺/稳妥/保底三种情景的上岸概率与依据；"
                + "3) 不同时间分配方案的提分预估；4) 风险提示与行动建议。语气客观、数据化，避免空泛鼓励。";
        String prompt = "【目标院校】" + school + "（" + major + "）\n"
                + "【当前估分】" + currentScore + " 【目标分数线】" + targetScore + "\n"
                + "【每日可投入】" + dailyHours + " 小时 【备考阶段】" + stage + "\n"
                + "【自认薄弱科目】" + weakSubjects + "\n"
                + "【我的学习画像】" + portrait;

        String report;
        boolean aiGenerated = true;
        try {
            report = deepSeekApi.sendDeepseekChat(system, prompt);
        } catch (Exception e) {
            log.warn("决策推演 AI 调用失败，使用本地推演模板", e);
            aiGenerated = false;
            report = localSimulateReport(school, major, currentScore, targetScore, dailyHours, stage, weakSubjects, scenario);
        }

        // 写入 AI 长期记忆
        AiMemory memory = saveMemory(user.getId(), "simulate",
                "择校推演 · " + school, report);
        Map<String, Object> result = new HashMap<>();
        result.put("report", report);
        result.put("scenarios", scenario);
        result.put("memoryId", memory.getId());
        result.put("aiGenerated", aiGenerated);
        return ResultUtils.success(result);
    }

    /**
     * 冲稳保三情景的概率启发式估计（AI 不可用时也能给出量化结果）
     */
    private Map<String, Object> buildScenarios(int currentScore, int targetScore, int dailyHours) {
        long daysLeft = Math.max(1, ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.of(2026, 12, 19)));
        // 粗略提分模型：每日小时数 * 剩余天数 * 效率系数
        double potential = dailyHours * daysLeft * 0.045;
        Map<String, Object> map = new HashMap<>();
        map.put("daysLeft", daysLeft);
        map.put("rush", scenario("冲刺情景", targetScore + 20, currentScore, potential));
        map.put("steady", scenario("稳妥情景", targetScore, currentScore, potential));
        map.put("safe", scenario("保底情景", targetScore - 20, currentScore, potential));
        return map;
    }

    private Map<String, Object> scenario(String name, int line, int current, double potential) {
        double expected = current + potential;
        int prob = (int) Math.round(100.0 / (1.0 + Math.exp(-(expected - line) / 18.0)));
        prob = Math.max(3, Math.min(97, prob));
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("line", line);
        m.put("expected", (int) Math.round(expected));
        m.put("probability", prob);
        return m;
    }

    private String localSimulateReport(String school, String major, int current, int target,
                                       int dailyHours, String stage, String weakSubjects,
                                       Map<String, Object> sc) {
        @SuppressWarnings("unchecked")
        Map<String, Object> rush = (Map<String, Object>) sc.get("rush");
        @SuppressWarnings("unchecked")
        Map<String, Object> steady = (Map<String, Object>) sc.get("steady");
        @SuppressWarnings("unchecked")
        Map<String, Object> safe = (Map<String, Object>) sc.get("safe");
        int gap = target - current;
        return "## 择校决策推演报告 · " + school + "\n\n"
                + "### 一、差距诊断\n\n"
                + "- 目标专业：" + major + "，目标分数线约 **" + target + "** 分\n"
                + "- 当前估分 **" + current + "** 分，差距 **" + gap + "** 分\n"
                + "- 距初试约 **" + sc.get("daysLeft") + "** 天，处于「" + stage + "」，每日可投入 **" + dailyHours + "** 小时\n\n"
                + "### 二、多情景上岸概率推演\n\n"
                + "| 情景 | 目标线 | 预计可达 | 上岸概率 |\n| --- | --- | --- | --- |\n"
                + row(rush) + row(steady) + row(safe) + "\n"
                + "### 三、时间分配建议\n\n"
                + "- 薄弱科目（" + weakSubjects + "）建议分配每日 40% 以上的时间做靶向补强\n"
                + "- 数学与 408 是拉分主战场，保持每日各 1.5~2 小时的连续训练\n"
                + "- 每周留半天复盘错题本，避免无差别刷题\n\n"
                + "### 四、风险提示\n\n"
                + (gap > 60 ? "- 当前差距偏大，建议同步准备一所保底院校，降低单点失败风险\n" : "- 差距处于可追赶区间，关键在于保持节奏、避免中后期心态波动\n")
                + "- 推演基于当前画像数据，随着刷题与模考数据积累，建议每月重新推演一次\n\n"
                + "> 本报告由本地推演模型生成（AI 服务暂不可用），数据仅供参考。";
    }

    private String row(Map<String, Object> s) {
        return "| " + s.get("name") + " | " + s.get("line") + " 分 | " + s.get("expected") + " 分 | **" + s.get("probability") + "%** |\n";
    }

    // ================= 个性化学习规划 =================

    @PostMapping("/plan/generate")
    public BaseResponse<Map<String, Object>> generatePlan(@RequestBody Map<String, Object> body,
                                                          HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        String stage = StringUtils.defaultIfBlank(String.valueOf(body.getOrDefault("stage", "")), "强化阶段");
        int dailyMinutes = parseInt(body.get("dailyMinutes"), 360);
        String subjects = StringUtils.defaultIfBlank(String.valueOf(body.getOrDefault("subjects", "")),
                "数据结构,计算机组成原理,操作系统,计算机网络");
        String goal = StringUtils.defaultIfBlank(String.valueOf(body.getOrDefault("goal", "")), "稳步提分");
        String targetDate = StringUtils.defaultIfBlank(String.valueOf(body.getOrDefault("targetDate", "")), "2026-12-19");
        long daysLeft = Math.max(1, ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(targetDate)));

        String portrait = portraitService.portraitText(user.getId());
        String system = "你是考研规划师。基于用户画像数据生成动态、可执行的分阶段复习计划，"
                + "覆盖基础/强化/冲刺/复试阶段的任务安排、时间分配、里程碑与自检方式。"
                + "输出 markdown，计划必须贴合用户当前薄弱点，而不是千人一面的模板。";
        String prompt = "【距初试】" + daysLeft + " 天 【当前阶段】" + stage + "\n"
                + "【每日可投入】" + dailyMinutes + " 分钟 【复习科目】" + subjects + "\n"
                + "【阶段目标】" + goal + "\n【我的学习画像】" + portrait;

        String plan;
        boolean aiGenerated = true;
        try {
            plan = deepSeekApi.sendDeepseekChat(system, prompt);
        } catch (Exception e) {
            log.warn("规划生成 AI 调用失败，使用本地模板", e);
            aiGenerated = false;
            plan = localPlan(daysLeft, stage, dailyMinutes, subjects, goal);
        }
        AiMemory memory = saveMemory(user.getId(), "plan", "个性化学习规划 · " + stage, plan);
        Map<String, Object> result = new HashMap<>();
        result.put("plan", plan);
        result.put("memoryId", memory.getId());
        result.put("aiGenerated", aiGenerated);
        result.put("daysLeft", daysLeft);
        return ResultUtils.success(result);
    }

    private String localPlan(long daysLeft, String stage, int dailyMinutes, String subjects, String goal) {
        int h = Math.max(1, dailyMinutes / 60);
        return "## 个性化学习规划（距初试 " + daysLeft + " 天）\n\n"
                + "### 阶段一 · 基础巩固（第 1-" + Math.max(1, daysLeft / 4) + " 天）\n\n"
                + "- 每日 " + h + " 小时：" + subjects.replace(",", "、") + " 按章节轮换\n"
                + "- 每学完一章立即做对应题库练习，错题当天入本复盘\n"
                + "- 里程碑：四科知识框架能脱稿复述\n\n"
                + "### 阶段二 · 强化突破\n\n"
                + "- 以真题与薄弱标签为主线，针对画像中的薄弱点做专项训练\n"
                + "- 每周一次限时套卷，记录分数曲线\n\n"
                + "### 阶段三 · 冲刺模考\n\n"
                + "- 隔日一套全真模拟，当天完成错题归因\n"
                + "- 政治/英语作文模板进入滚动背诵\n\n"
                + "### 阶段四 · 复试衔接\n\n"
                + "- 初试后两周内启动复试专业课与英语口语准备\n"
                + "- 用简历工坊整理项目经历，准备自我介绍\n\n"
                + "> 阶段目标：" + goal + "。本计划由本地模板生成（AI 服务暂不可用），联网后可生成更贴合画像的版本。";
    }

    // ================= AI 画像总结 =================

    @PostMapping("/portrait/summary")
    public BaseResponse<Map<String, Object>> portraitSummary(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        String portrait = portraitService.portraitText(user.getId());
        String system = "你是学习诊断顾问。把用户的学习画像数据转写成一段 150 字以内、具体、温和但诚实的学情总结，"
                + "指出 1-2 个最值得关注的薄弱点和 1 个可以保持的优势，避免套话。";
        String summary;
        boolean aiGenerated = true;
        try {
            summary = deepSeekApi.sendDeepseekChat(system, portrait);
        } catch (Exception e) {
            log.warn("画像总结 AI 调用失败", e);
            aiGenerated = false;
            summary = "你的学习数据正在慢慢积累：" + portrait + " 建议保持每日打卡与错题复盘，"
                    + "画像会随着你的每一次练习变得更准确。";
        }
        saveMemory(user.getId(), "portrait", "画像快照 · " + LocalDate.now(), summary);
        Map<String, Object> result = new HashMap<>();
        result.put("summary", summary);
        result.put("aiGenerated", aiGenerated);
        return ResultUtils.success(result);
    }

    // ================= AI 长期记忆存取 =================

    @GetMapping("/memory")
    public BaseResponse<List<AiMemory>> memories(@RequestParam(required = false) String type,
                                                 HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        List<AiMemory> list = aiMemoryMapper.selectList(new QueryWrapper<AiMemory>()
                .eq("userId", user.getId())
                .eq(StringUtils.isNotBlank(type), "memoryType", type)
                .orderByDesc("id").last("limit 50"));
        return ResultUtils.success(list);
    }

    @DeleteMapping("/memory/{id}")
    public BaseResponse<Boolean> deleteMemory(@PathVariable Long id, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        AiMemory memory = aiMemoryMapper.selectById(id);
        ThrowUtils.throwIf(memory == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(!memory.getUserId().equals(user.getId()) && !userService.isAdmin(request),
                ErrorCode.NO_AUTH_ERROR);
        return ResultUtils.success(aiMemoryMapper.deleteById(id) > 0);
    }

    private AiMemory saveMemory(Long userId, String type, String title, String content) {
        AiMemory memory = new AiMemory();
        memory.setUserId(userId);
        memory.setMemoryType(type);
        memory.setTitle(title);
        memory.setContent(content);
        aiMemoryMapper.insert(memory);
        return memory;
    }

    private int parseInt(Object o, int def) {
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception e) {
            return def;
        }
    }
}
