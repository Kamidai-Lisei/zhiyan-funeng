package com.jay.aigcoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jay.aigcoj.common.ErrorCode;
import com.jay.aigcoj.exception.BusinessException;
import com.jay.aigcoj.mapper.CodeAnalysisMapper;
import com.jay.aigcoj.model.entity.CodeAnalysis;
import com.jay.aigcoj.service.CodeAnalysisService;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码分析服务实现类
 */
@Service
public class CodeAnalysisServiceImpl extends ServiceImpl<CodeAnalysisMapper, CodeAnalysis> implements CodeAnalysisService {

    private static final Pattern SCORE_PATTERN = Pattern.compile("代码评分[：:](\\s*)([0-9]+)(\\s*)[分/]");

    @Override
    public long saveAnalysisResult(Long questionSubmitId, String analysisResult) {
        if (questionSubmitId == null || analysisResult == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        // 尝试从分析结果中提取分数
        Integer score = extractScore(analysisResult);
        
        // 检查是否已存在分析结果
        CodeAnalysis existAnalysis = getOneByQuestionSubmitId(questionSubmitId);
        
        if (existAnalysis != null) {
            // 更新已有记录
            existAnalysis.setAnalysisResult(analysisResult);
            if (score != null) {
                existAnalysis.setCodeScore(score);
            }
            boolean updateResult = this.updateById(existAnalysis);
            if (!updateResult) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新分析结果失败");
            }
            return existAnalysis.getId();
        } else {
            // 创建新记录
            CodeAnalysis codeAnalysis = new CodeAnalysis();
            codeAnalysis.setQuestionSubmitId(questionSubmitId);
            codeAnalysis.setAnalysisResult(analysisResult);
            if (score != null) {
                codeAnalysis.setCodeScore(score);
            }
            boolean saveResult = this.save(codeAnalysis);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存分析结果失败");
            }
            return codeAnalysis.getId();
        }
    }

    @Override
    public String getAnalysisResult(Long questionSubmitId) {
        if (questionSubmitId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        CodeAnalysis codeAnalysis = getOneByQuestionSubmitId(questionSubmitId);
        if (codeAnalysis == null) {
            return null;
        }
        
        return codeAnalysis.getAnalysisResult();
    }

    @Override
    public boolean hasAnalysisResult(Long questionSubmitId) {
        if (questionSubmitId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        CodeAnalysis codeAnalysis = getOneByQuestionSubmitId(questionSubmitId);
        return codeAnalysis != null;
    }
    
    /**
     * 根据题目提交ID获取分析结果
     */
    private CodeAnalysis getOneByQuestionSubmitId(Long questionSubmitId) {
        QueryWrapper<CodeAnalysis> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("question_submit_id", questionSubmitId);
        return this.getOne(queryWrapper);
    }
    
    /**
     * 从分析结果中提取分数
     */
    private Integer extractScore(String analysisResult) {
        if (analysisResult == null || analysisResult.isEmpty()) {
            return null;
        }
        
        Matcher matcher = SCORE_PATTERN.matcher(analysisResult);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(2).trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        
        return null;
    }
} 