package com.jay.aigcoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 题目收藏 / 错题本
 */
@TableName("question_favour")
@Data
public class QuestionFavour implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long questionId;

    private Long userId;

    private Date createTime;

    private static final long serialVersionUID = 1L;
}
