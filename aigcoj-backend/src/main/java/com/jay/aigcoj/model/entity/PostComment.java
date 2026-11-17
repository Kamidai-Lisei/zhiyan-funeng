package com.jay.aigcoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 帖子评论
 */
@TableName("post_comment")
@Data
public class PostComment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private Long userId;

    private String content;

    private Long parentId;

    private Date createTime;

    @TableLogic
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}
