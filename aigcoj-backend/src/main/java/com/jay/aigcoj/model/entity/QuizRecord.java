package com.jay.aigcoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 408 习题作答记录
 */
@TableName("quiz_record")
@Data
public class QuizRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long questionId;

    private String chosen;

    /**
     * 1 答对 / 0 答错
     */
    private Integer correct;

    private Date createTime;

    private static final long serialVersionUID = 1L;
}
