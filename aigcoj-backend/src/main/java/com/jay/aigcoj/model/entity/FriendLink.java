package com.jay.aigcoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 好友关系
 */
@TableName("friend_link")
@Data
public class FriendLink implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发起方用户 id
     */
    private Long userId;

    /**
     * 接收方用户 id
     */
    private Long friendId;

    /**
     * 0 待确认 / 1 已互为好友
     */
    private Integer status;

    private String remark;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
