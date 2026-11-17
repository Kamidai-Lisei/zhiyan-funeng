package com.jay.aigcoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 408 章节学习进度
 */
@TableName("study_progress")
@Data
public class StudyProgress implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String module;

    private String slug;

    private Integer finished;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
