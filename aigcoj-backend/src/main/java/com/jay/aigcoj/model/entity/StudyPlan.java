package com.jay.aigcoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 学习计划
 */
@TableName("study_plan")
@Data
public class StudyPlan implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Date targetDate;

    private Integer dailyMinutes;

    private String subjects;

    private String motto;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
