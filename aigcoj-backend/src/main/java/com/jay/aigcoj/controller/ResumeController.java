package com.jay.aigcoj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jay.aigcoj.bot.ds.DSApi;
import com.jay.aigcoj.common.BaseResponse;
import com.jay.aigcoj.common.ErrorCode;
import com.jay.aigcoj.common.ResultUtils;
import com.jay.aigcoj.exception.BusinessException;
import com.jay.aigcoj.exception.ThrowUtils;
import com.jay.aigcoj.mapper.ResumeAnalysisMapper;
import com.jay.aigcoj.mapper.ResumeMapper;
import com.jay.aigcoj.model.entity.Resume;
import com.jay.aigcoj.model.entity.ResumeAnalysis;
import com.jay.aigcoj.model.entity.User;
import com.jay.aigcoj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简历辅导
 */
@RestController
@RequestMapping("/resume")
@Slf4j
public class ResumeController {

    @Resource
    private ResumeMapper resumeMapper;

    @Resource
    private ResumeAnalysisMapper analysisMapper;

    @Resource
    private UserService userService;

    @Resource
    private DSApi deepSeekApi;

    @GetMapping("/latest")
    public BaseResponse<Resume> latest(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        Resume resume = resumeMapper.selectOne(new QueryWrapper<Resume>()
                .eq("userId", user.getId()).orderByDesc("id").last("limit 1"));
        if (resume == null) {
            resume = new Resume();
            resume.setUserId(user.getId());
            resume.setTitle(user.getUserName() == null ? "我的简历" : user.getUserName() + "的简历");
            resume.setTargetSchool("");
            resume.setTargetMajor("计算机科学与技术");
            resume.setTemplate("classic");
            resume.setScore(0);
            resume.setContent("{}");
            resumeMapper.insert(resume);
        }
        return ResultUtils.success(resume);
    }

    @GetMapping("/list")
    public BaseResponse<List<Resume>> list(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(resumeMapper.selectList(new QueryWrapper<Resume>()
                .eq("userId", user.getId()).orderByDesc("id")));
    }

    @PostMapping("/save")
    public BaseResponse<Long> save(@RequestBody Resume body, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        ThrowUtils.throwIf(body == null || StringUtils.isBlank(body.getTitle()), ErrorCode.PARAMS_ERROR, "标题不能为空");
        body.setUserId(user.getId());
        if (body.getId() == null) {
            resumeMapper.insert(body);
        } else {
            Resume existed = resumeMapper.selectById(body.getId());
            ThrowUtils.throwIf(existed == null || !existed.getUserId().equals(user.getId()),
                    ErrorCode.NO_AUTH_ERROR, "无权修改该简历");
            resumeMapper.updateById(body);
        }
        return ResultUtils.success(body.getId());
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> delete(@RequestBody Map<String, Long> body, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        Long id = body.get("id");
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        Resume resume = resumeMapper.selectById(id);
        ThrowUtils.throwIf(resume == null || !resume.getUserId().equals(user.getId()), ErrorCode.NO_AUTH_ERROR);
        return ResultUtils.success(resumeMapper.deleteById(id) > 0);
    }

    @GetMapping("/templates")
    public BaseResponse<List<Map<String, String>>> templates() {
        List<Map<String, String>> list = new ArrayList<>();
        list.add(template("classic", "经典学术", "适合复试与科研方向，稳重清晰"));
        list.add(template("modern", "现代简约", "适合互联网求职，重点突出项目"));
        list.add(template("tech", "技术极简", "适合后端/算法岗，强调技术栈与产出"));
        return ResultUtils.success(list);
    }

    private Map<String, String> template(String id, String name, String desc) {
        Map<String, String> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("desc", desc);
        return map;
    }

    @PostMapping("/analyze")
    public BaseResponse<Map<String, Object>> analyze(@RequestBody Map<String, Long> body, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        Long id = body.get("id");
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        Resume resume = resumeMapper.selectById(id);
        ThrowUtils.throwIf(resume == null || !resume.getUserId().equals(user.getId()),
                ErrorCode.NO_AUTH_ERROR, "无权分析该简历");

        String content = StringUtils.defaultString(resume.getContent(), "{}");
        if (content.length() > 6000) {
            content = content.substring(0, 6000);
        }
        String prompt = "你是一位资深的计算机考研复试与互联网求职简历顾问。请诊断下面这份学生简历。\n" +
                "目标院校：" + StringUtils.defaultString(resume.getTargetSchool(), "未填写") + "\n" +
                "目标专业：" + StringUtils.defaultString(resume.getTargetMajor(), "计算机") + "\n" +
                "简历内容（JSON）：\n" + content + "\n\n" +
                "请严格按以下结构输出，语言精炼、可执行：\n" +
                "评分：<0-100 的整数>\n" +
                "## 总体评价\n## 三大亮点\n## 主要问题\n## 逐条修改建议\n## 复试/求职匹配度\n";
        String result;
        try {
            result = deepSeekApi.sendDeepseekChat(
                    "你是计算机考研与求职领域的简历专家，回答要具体、务实、可落地。", prompt);
        } catch (Exception e) {
            log.warn("AI 简历诊断失败，使用本地规则引擎", e);
            result = localAnalysis(resume);
        }
        int score = parseScore(result);
        if (score <= 0) {
            score = 72;
        }
        ResumeAnalysis analysis = new ResumeAnalysis();
        analysis.setResumeId(id);
        analysis.setUserId(user.getId());
        analysis.setScore(score);
        analysis.setResult(result);
        analysisMapper.insert(analysis);

        resume.setScore(score);
        resumeMapper.updateById(resume);

        Map<String, Object> data = new HashMap<>();
        data.put("score", score);
        data.put("result", result);
        return ResultUtils.success(data);
    }

    @GetMapping("/analysis")
    public BaseResponse<ResumeAnalysis> latestAnalysis(@RequestParam long id, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        ResumeAnalysis analysis = analysisMapper.selectOne(new QueryWrapper<ResumeAnalysis>()
                .eq("resumeId", id).eq("userId", user.getId()).orderByDesc("id").last("limit 1"));
        return ResultUtils.success(analysis);
    }

    /**
     * 与 AI 对话修改简历：返回建议；若 AI 给出修改后的简历 JSON 则同步落库
     * 请求体：{resumeId, message, history:[{role, content}]}
     */
    @PostMapping("/chat")
    public BaseResponse<Map<String, Object>> chat(@RequestBody Map<String, Object> body,
                                                  HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        Long resumeId = body.get("resumeId") == null ? null
                : Long.valueOf(String.valueOf(body.get("resumeId")));
        String message = StringUtils.trimToEmpty(String.valueOf(body.getOrDefault("message", "")));
        ThrowUtils.throwIf(resumeId == null || StringUtils.isEmpty(message),
                ErrorCode.PARAMS_ERROR, "缺少简历或修改意见");
        Resume resume = resumeMapper.selectById(resumeId);
        ThrowUtils.throwIf(resume == null || !resume.getUserId().equals(user.getId()),
                ErrorCode.NO_AUTH_ERROR, "无权修改该简历");

        StringBuilder conv = new StringBuilder();
        Object rawHistory = body.get("history");
        if (rawHistory instanceof List) {
            int count = 0;
            for (Object o : (List<?>) rawHistory) {
                if (o instanceof Map && count < 10) {
                    String role = StringUtils.trimToEmpty(String.valueOf(((Map<?, ?>) o).get("role")));
                    String content = StringUtils.trimToEmpty(String.valueOf(((Map<?, ?>) o).get("content")));
                    if (StringUtils.isNotBlank(content)) {
                        conv.append(role.contains("assistant") || role.contains("ai") ? "AI：" : "用户：")
                                .append(content.length() > 500 ? content.substring(0, 500) : content)
                                .append('\n');
                        count++;
                    }
                }
            }
        }
        String content = StringUtils.defaultString(resume.getContent(), "{}");
        if (content.length() > 5000) {
            content = content.substring(0, 5000);
        }
        String prompt = "当前简历内容（JSON，字段含 basic/experience/projects/skills 等）：\n" + content +
                (conv.length() > 0 ? "\n\n最近的对话记录：\n" + conv : "") +
                "\n用户这次的修改意见：" + message + "\n\n" +
                "请完成两件事：\n" +
                "1. 用简洁的 markdown（可用列表）给出针对该简历的具体修改建议与面试/求职指导；\n" +
                "2. 如果意见涉及修改简历内容本身，请输出修改后的完整简历 JSON，放在如下标记中：\n" +
                "```resume-json\n{...完整的简历 JSON...}\n```\n" +
                "若无需改动简历内容，则不要输出该标记。保持 JSON 字段结构与原简历一致。";
        String reply;
        try {
            reply = deepSeekApi.sendDeepseekChat(
                    "你是资深的计算机考研复试与互联网求职简历顾问，擅长通过对话式辅导帮学生打磨简历，回答具体、可执行。",
                    prompt);
        } catch (Exception e) {
            log.warn("AI 简历对话失败", e);
            reply = "AI 服务暂时不可用，请稍后重试。也可以参考通用建议：项目经历补充量化指标、技能按熟练度排序、突出与目标岗位匹配的关键词。";
        }
        ThrowUtils.throwIf(StringUtils.isBlank(reply), ErrorCode.SYSTEM_ERROR, "AI 未返回内容");

        // 提取修改后的简历 JSON（若存在）
        Map<String, Object> data = new HashMap<>();
        data.put("reply", reply);
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("```resume-json\\s*([\\s\\S]*?)```").matcher(reply);
        if (m.find()) {
            try {
                String json = m.group(1).trim();
                new com.google.gson.JsonParser().parse(json);
                resume.setContent(json);
                resumeMapper.updateById(resume);
                data.put("resumeJson", json);
                data.put("applied", true);
            } catch (Exception e) {
                log.warn("AI 返回的简历 JSON 解析失败", e);
            }
        }
        return ResultUtils.success(data);
    }

    private int parseScore(String text) {
        if (StringUtils.isBlank(text)) {
            return 0;
        }
        Matcher matcher = Pattern.compile("(?:评分|得分|总分)[：:\\s]*([0-9]{1,3})").matcher(text);
        if (matcher.find()) {
            return Math.min(100, Integer.parseInt(matcher.group(1)));
        }
        matcher = Pattern.compile("([0-9]{2,3})\\s*[/／]\\s*100").matcher(text);
        if (matcher.find()) {
            return Math.min(100, Integer.parseInt(matcher.group(1)));
        }
        return 0;
    }

    private String localAnalysis(Resume resume) {
        String content = StringUtils.defaultString(resume.getContent());
        int score = 60;
        StringBuilder sb = new StringBuilder();
        if (content.contains("项目")) score += 8;
        if (content.contains("实习")) score += 8;
        if (content.contains("408") || content.contains("考研")) score += 5;
        if (content.contains("技能")) score += 4;
        score = Math.min(score, 92);
        sb.append("评分：").append(score).append("\n\n");
        sb.append("## 总体评价\n简历结构基本完整，建议进一步量化项目成果，突出与目标院校/岗位的匹配度。\n\n");
        sb.append("## 三大亮点\n- 有明确的目标专业与考研方向\n- 具备计算机专业基础课程背景\n- 有可继续深挖的项目经历\n\n");
        sb.append("## 主要问题\n- 项目描述缺少数据指标\n- 技术栈与岗位关键词覆盖不足\n- 教育经历之外的个人特色不够鲜明\n\n");
        sb.append("## 逐条修改建议\n1. 每个项目补充 1-2 个量化结果，例如性能提升、用户量、延迟下降。\n");
        sb.append("2. 在技能栏补充 408 基础、语言、框架与工具，按熟练度排序。\n");
        sb.append("3. 用 STAR 法则重写项目：背景、任务、行动、结果。\n\n");
        sb.append("## 复试/求职匹配度\n当前匹配度中等偏上，补齐项目量化与专业关键词后可显著提升。\n");
        return sb.toString();
    }
}
