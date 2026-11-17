package com.jay.aigcoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 学习打卡
 */
@TableName("study_checkin")
@Data
public class StudyCheckin implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Date checkinDate;

    private Integer duration;

    private String content;

    private Date createTime;

    private static final long serialVersionUID = 1L;
}
