package com.jay.aigcoj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jay.aigcoj.model.entity.CodeAnalysis;

/**
 * 代码分析服务
 */
public interface CodeAnalysisService extends IService<CodeAnalysis> {

    /**
     * 保存代码分析结果
     * @param questionSubmitId 题目提交ID
     * @param analysisResult 分析结果
     * @return 保存的分析结果ID
     */
    long saveAnalysisResult(Long questionSubmitId, String analysisResult);

    /**
     * 获取代码分析结果
     * @param questionSubmitId 题目提交ID
     * @return 分析结果
     */
    String getAnalysisResult(Long questionSubmitId);

    /**
     * 判断是否已有分析结果
     * @param questionSubmitId 题目提交ID
     * @return 是否存在
     */
    boolean hasAnalysisResult(Long questionSubmitId);
} 