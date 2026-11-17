package com.jay.aigcoj.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 代码分析结果实体
 */
@TableName(value = "code_analysis")
@Data
public class CodeAnalysis implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 题目提交ID
     */
    @TableField(value = "question_submit_id")
    private Long questionSubmitId;

    /**
     * 分析结果
     */
    @TableField(value = "analysis_result")
    private String analysisResult;

    /**
     * 代码评分
     */
    @TableField(value = "code_score")
    private Integer codeScore;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
} 