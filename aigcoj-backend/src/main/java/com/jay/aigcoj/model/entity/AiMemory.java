package com.jay.aigcoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * AI 长期记忆：用户画像快照、决策推演报告、个性化规划等持续沉淀的个人备考档案
 */
@TableName("ai_memory")
@Data
public class AiMemory implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * portrait 画像快照 / simulate 决策推演 / plan 学习规划 / note 学习笔记
     */
    private String memoryType;

    private String title;

    private String content;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
