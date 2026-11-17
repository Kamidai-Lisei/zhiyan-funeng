package com.jay.aigcoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jay.aigcoj.common.ErrorCode;
import com.jay.aigcoj.constant.CommonConstant;
import com.jay.aigcoj.exception.BusinessException;
import com.jay.aigcoj.judge.JudgeService;
import com.jay.aigcoj.mapper.QuestionSubmitMapper;
import com.jay.aigcoj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.jay.aigcoj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.jay.aigcoj.model.entity.Question;
import com.jay.aigcoj.model.entity.QuestionSubmit;
import com.jay.aigcoj.model.entity.Switch;
import com.jay.aigcoj.model.entity.User;
import com.jay.aigcoj.model.enums.QuestionSubmitLanguageEnum;
import com.jay.aigcoj.model.enums.QuestionSubmitStatusEnum;
import com.jay.aigcoj.model.vo.QuestionSubmitVO;
import com.jay.aigcoj.model.vo.QuestionVO;
import com.jay.aigcoj.service.QuestionService;
import com.jay.aigcoj.service.QuestionSubmitService;
import com.jay.aigcoj.service.UserService;
import com.jay.aigcoj.utils.SqlUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
* @description 针对表【question_submit(题目提交)】的数据库操作Service实现
*/
@Service
@Slf4j
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
    implements QuestionSubmitService{
    
    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    @Lazy
    private JudgeService judgeService;

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }

        long questionId = questionSubmitAddRequest.getQuestionId();
        // 判断实体是否存在，根据类别获取实体
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已提交题目
        long userId = loginUser.getId();
        // 每个用户串行提交题目
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        questionSubmit.setLanguage(language);
        // 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        boolean save = this.save(questionSubmit);
        if (!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "数据插入失败");
        }
        Long questionSubmitId = questionSubmit.getId();
        // 执行判题服务
        CompletableFuture.runAsync(() -> {
            try {
                judgeService.doJudge(questionSubmitId);
            } catch (Throwable t) {
                log.error("判题失败, submitId={}", questionSubmitId, t);
                QuestionSubmit failed = new QuestionSubmit();
                failed.setId(questionSubmitId);
                failed.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
                failed.setJudgeInfo("{\"message\":\"系统判题异常：" + (t.getMessage() == null ? "未知错误" : t.getMessage().replace("\"", "'")) + "\"}");
                this.updateById(failed);
            }
        });
        return questionSubmitId;
    }


    /**
     * 获取查询包装类（用户根据哪些字段查询，根据前端传来的请求对象，得到 mybatis 框架支持的查询 QueryWrapper 类）
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        String language = questionSubmitQueryRequest.getLanguage();
        Integer status = questionSubmitQueryRequest.getStatus();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(StringUtils.isNotBlank(language), "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.eq("isDelete", false);
        if (SqlUtils.validSortField(sortField)) {
            queryWrapper.orderBy(true, sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        } else {
            // 默认按 id 倒序（最新提交在前），否则分页第一页全是旧数据，前端轮询新提交会找不到
            queryWrapper.orderBy(true, false, "id");
        }
        return queryWrapper;
    }

    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        // 脱敏：仅本人和管理员能看见自己（提交 userId 和登录用户 id 不同）提交的代码
        long userId = loginUser.getId();
        // 处理脱敏
        if (userId != questionSubmit.getUserId() && !userService.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }
        return questionSubmitVO;
    }

    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser) {
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        // 批量填充题目信息，避免前端只拿到题目 id
        List<Long> questionIds = questionSubmitList.stream()
                .map(QuestionSubmit::getQuestionId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Question> questionMap = questionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream()
                .map(questionSubmit -> {
                    QuestionSubmitVO vo = getQuestionSubmitVO(questionSubmit, loginUser);
                    Question question = questionMap.get(questionSubmit.getQuestionId());
                    if (question != null) {
                        QuestionVO questionVO = QuestionVO.objToVo(question);
                        questionVO.setContent(null);
                        vo.setQuestionVO(questionVO);
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }

    @Override
    public String getCode(Long questionSubmitId) {
        QueryWrapper<QuestionSubmit> queryWrapper =new QueryWrapper<>();
        queryWrapper.eq("id",questionSubmitId);
        QuestionSubmit questionSubmit = this.baseMapper.selectOne(queryWrapper);
        return questionSubmit.getCode();
    }

}




