package com.jay.aigcoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 408 在线习题（选择题）
 */
@TableName("quiz_question")
@Data
public class QuizQuestion implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * ds 数据结构 / co 组成原理 / os 操作系统 / cn 计算机网络
     */
    private String subject;

    private String chapter;

    /**
     * 来源：章节练习 / 2023 真题 等
     */
    private String source;

    private String question;

    /**
     * 选项 JSON 数组，如 ["A. xxx","B. xxx"]
     */
    private String options;

    /**
     * 正确选项字母，如 A
     */
    private String answer;

    private String analysis;

    private Date createTime;

    private static final long serialVersionUID = 1L;
}
