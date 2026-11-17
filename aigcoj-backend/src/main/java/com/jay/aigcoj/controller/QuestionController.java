package com.jay.aigcoj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.jay.aigcoj.annotation.AuthCheck;
import com.jay.aigcoj.common.BaseResponse;
import com.jay.aigcoj.common.DeleteRequest;
import com.jay.aigcoj.common.ErrorCode;
import com.jay.aigcoj.common.ResultUtils;
import com.jay.aigcoj.constant.UserConstant;
import com.jay.aigcoj.exception.BusinessException;
import com.jay.aigcoj.exception.ThrowUtils;
import com.jay.aigcoj.model.dto.question.*;
import com.jay.aigcoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.jay.aigcoj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.jay.aigcoj.model.entity.Question;
import com.jay.aigcoj.model.entity.QuestionSubmit;
import com.jay.aigcoj.model.entity.User;
import com.jay.aigcoj.model.vo.QuestionSubmitVO;
import com.jay.aigcoj.model.vo.QuestionVO;
import com.jay.aigcoj.service.QuestionService;
import com.jay.aigcoj.service.QuestionSubmitService;
import com.jay.aigcoj.service.UserService;
import com.jay.aigcoj.service.CodeAnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.jay.aigcoj.bot.ds.DSApi;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 题目接口
 * 
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private DSApi deepSeekApi;

    @Resource
    private CodeAnalysisService codeAnalysisService;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        if (questionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        List<String> tags = questionAddRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionAddRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionAddRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        questionService.validQuestion(question, true);
        User loginUser = userService.getLoginUser(request);
        question.setUserId(loginUser.getId());
        question.setFavourNum(0);
        question.setThumbNum(0);
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newQuestionId = question.getId();
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = questionService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        List<String> tags = questionUpdateRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionUpdateRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionUpdateRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        long id = questionUpdateRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = questionService.updateById(question);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Question> getQuestionById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        // 不是本人或管理员，不能直接获取所有信息
        if (!question.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return ResultUtils.success(question);
    }

    /**
     * 根据 id 获取（脱敏）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(questionService.getQuestionVO(question, request));
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
            HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
            HttpServletRequest request) {
        if (questionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        questionQueryRequest.setUserId(loginUser.getId());
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取题目列表（仅管理员）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                   HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionPage);
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request) {
        if (questionEditRequest == null || questionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        List<String> tags = questionEditRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionEditRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionEditRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        User loginUser = userService.getLoginUser(request);
        long id = questionEditRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = questionService.updateById(question);
        return ResultUtils.success(result);
    }

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return 提交记录的 id
     */
    @PostMapping("/question_submit/do")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                               HttpServletRequest request) {
        // 检查请求参数是否有效
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能提交问题
        final User loginUser = userService.getLoginUser(request);
        // 处理问题提交逻辑并获取提交记录ID
        long questionSubmitId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        // 返回成功响应，包含提交记录ID
        return ResultUtils.success(questionSubmitId);
    }

    /**
     * 分页获取题目提交列表（除了管理员外，普通用户只能看到非答案、提交代码等公开信息）
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/question_submit/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                         HttpServletRequest request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        // 从数据库中查询原始的题目提交分页信息
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        final User loginUser = userService.getLoginUser(request);
        // 返回脱敏信息
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, loginUser));
    }

    @GetMapping("/question_submit/getCode")
    public BaseResponse<String> getCode(@RequestParam("questionSubmitId") Long questionSubmitId) {

        return ResultUtils.success(questionSubmitService.getCode(questionSubmitId));
    }

    /**
     * 当前用户已通过（Accepted）的题目 id 列表，用于题库页展示"我的状态"
     */
    @GetMapping("/question_submit/solved")
    public BaseResponse<List<String>> solvedQuestionIds(HttpServletRequest request) {
        final User loginUser = userService.getLoginUser(request);
        List<QuestionSubmit> submits = questionSubmitService.list(new QueryWrapper<QuestionSubmit>()
                .eq("userId", loginUser.getId())
                .eq("status", 2)
                .like("judgeInfo", "Accepted")
                .select("questionId")
                .groupBy("questionId"));
        List<String> ids = submits.stream()
                .map(s -> String.valueOf(s.getQuestionId()))
                .collect(java.util.stream.Collectors.toList());
        return ResultUtils.success(ids);
    }

    /**
     * 智能分析提交的代码
     *
     * @param questionSubmitId 提交记录ID
     * @return 分析结果
     */
    @GetMapping("/question_submit/analyzeCode")
    public BaseResponse<String> analyzeCode(@RequestParam("questionSubmitId") Long questionSubmitId) {
        // 检查是否已有分析结果
        if (codeAnalysisService.hasAnalysisResult(questionSubmitId)) {
            // 如果已有分析结果，直接返回
            String existingResult = codeAnalysisService.getAnalysisResult(questionSubmitId);
            return ResultUtils.success(existingResult);
        }

        // 获取提交的代码
        String code = questionSubmitService.getCode(questionSubmitId);
        if (code == null || code.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "代码不存在");
        }
        // 获取题目信息
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提交记录不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        String language = questionSubmit.getLanguage();
        // 获取题目名称和内容
        String questionName = questionService.getQuestionName(questionId);
        String questionContent = questionService.getQuestionContent(questionId);
        
        // 构建发送给 deepseek 的提示信息
        String prompt = "你需要根据用户提供的编程题类型、题目要求及考生代码，按照以下评分标准进行自动化评分。" +
                "评分需逻辑清晰、标准明确，针对不同题型动态调整评分权重，并给出具体扣分理由。（满分为100分）。\n" +
                "题目名称：" + questionName +
                "题目内容：" + questionContent +
                "提交的编程语言：" + language  +
                "提交的代码：\n```\n" + code + "\n```\n" +
                "请对这段代码进行分析并提供以下内容：" +
                "1. 代码综合评分结果（得分结果/满分）（评分标准：" +
                "一、正确性（70分）" +
                "1.功能完整性（30分）" +
                "- 未实现核心功能，代码无法运行或基本逻辑错误（例：题目要求排序算法，提交代码无限循环），得0-5分；" +
                "- 实现部分核心功能，但存在关键缺陷（例：排序算法仅对部分数据有效，无法处理重复元素），得10-15分" +
                "- 实现大部分功能，但存在次要缺陷（例：排序算法正确，但未处理空数组输入），得20-25分。" +
                "- 完全实现题目要求的所有功能（例：排序算法正确处理各种边界情况和异常输入），得30分" +
                "2.测试用例通过率（30分）" +
                "（1）基础用例（20分）" +
                "- 常规输入：覆盖题目描述的典型场景（例：计算两数之和，输入正数、零、负数组合），得5分" +
                "- 中间结果正确性：关键步骤的输出符合预期（例：递归函数的中间计算结果是否正确），得5分" +
                "- 最终结果正确性：程序输出与预期完全一致，得10分" +
                "（2）边界条件（10分）" +
                "- 极值处理：最大值、最小值、空值、零值等（例：数组长度为0/1时的处理），得3分" +
                "- 数据类型边界：溢出、精度丢失、类型转换等（例：整数除法是否考虑小数部分），得3分" +
                "- 逻辑边界：循环终止条件、递归出口等（例：二分查找是否正确处理左右边界相等的情况），得4分" +
                "3.逻辑正确性（10分）" +
                "- 必须使用符合题目要求的算法逻辑去完成要求（例：题目要求使用循环算法，代码中要使用循环算法中的for、if逻辑来完成）" +
                "二、算法效率（15分）" +
                "1.时间复杂度（10分）" +
                "- 指数级复杂度（如O(2ⁿ)）（例：冒泡排序处理大规模数据），得0-3分" +
                "- 多项式复杂度但非最优（如O(n²)）（例：快速排序实现存在缺陷导致性能下降），得4-6分" +
                "- 接近最优复杂度（如O(nlogn)）（例：标准快速排序实现），得7-9分" +
                "- 最优复杂度（如O(n)）（例：针对特定数据分布的线性时间排序），得10分" +
                "2.空间复杂度（5分）" +
                "- 额外空间使用：是否使用不必要的额外数据结构（例：原地排序算法使用O(1)空间得满分），占3分" +
                "- 内存泄漏风险：是否存在未释放的资源或内存溢出隐患（例：递归深度过大导致栈溢出），占2分" +
                "三、代码质量（15分）" +
                "1.代码可读性（8分）" +
                "- 变量/函数命名合理（例：所有命名清晰反映功能或含义即可），得2.5分" +
                "- 代码无明显语法错误，得2.5分" +
                "- 无重复代码，合理封装函数/类，得3分" +
                "2.规范性（6分）" +
                "- 缩进一致性（例：Python需严格遵守缩进规则），得3分" +
                "- 空格/空行使用：符合语言惯例（如Java的大括号位置），得3分）" +
                "2. 错误提示（如果存在错误）" +
                "3. 代码质量分析（简洁性、可读性、效率等）" +
                "4. 可能存在的错误或问题" +
                "5. 改进建议" +
                "请用中文回答，并确保输出格式整洁清晰。";
        
        try {
            String analysisResult = deepSeekApi.sendDeepseekChat(prompt);
            codeAnalysisService.saveAnalysisResult(questionSubmitId, analysisResult);
            return ResultUtils.success(analysisResult);
        } catch (UnirestException e) {
            log.error("调用 deepseek API 失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "智能分析服务暂时不可用");
        }
    }

    /**
     * 获取已有的代码分析结果
     *
     * @param questionSubmitId 提交记录ID
     * @return 分析结果
     */
    @GetMapping("/question_submit/getAnalysisResult")
    public BaseResponse<String> getAnalysisResult(@RequestParam("questionSubmitId") Long questionSubmitId) {
        // 参数校验
        if (questionSubmitId == null || questionSubmitId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取分析结果
        String analysisResult = codeAnalysisService.getAnalysisResult(questionSubmitId);
        if (analysisResult == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "暂无分析结果");
        }
        return ResultUtils.success(analysisResult);
    }

    /**
     * 获取ID到名称的映射
     *
     * @param request
     * @return
     */
    @GetMapping("/mapping")
    public BaseResponse<Map<String, String>> getIdToNameMapping(HttpServletRequest request) {
        Map<String, String> mapping = new HashMap<>();
        // 获取问题ID到名称的映射
        List<Question> questions = questionService.list();
        for (Question question : questions) {
            mapping.put("q_" + question.getId(), question.getTitle());
        }

        // 获取用户ID到名称的映射
        List<User> users = userService.list();
        for (User user : users) {
            mapping.put("u_" + user.getId(), user.getUserName());
        }

        return ResultUtils.success(mapping);
    }
}
