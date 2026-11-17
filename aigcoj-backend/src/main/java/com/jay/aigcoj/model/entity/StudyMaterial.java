package com.jay.aigcoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 学习资料
 */
@TableName("study_material")
@Data
public class StudyMaterial implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private String category;

    private String subject;

    private String fileType;

    private String fileUrl;

    private String coverUrl;

    private Long fileSize;

    private Integer downloadNum;

    private Integer favourNum;

    private Integer viewNum;

    private String tags;

    private Long uploaderId;

    private Integer status;

    private Date createTime;

    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
