package com.jay.aigcoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 简历诊断记录
 */
@TableName("resume_analysis")
@Data
public class ResumeAnalysis implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long resumeId;

    private Long userId;

    private Integer score;

    private String result;

    private Date createTime;

    private static final long serialVersionUID = 1L;
}
