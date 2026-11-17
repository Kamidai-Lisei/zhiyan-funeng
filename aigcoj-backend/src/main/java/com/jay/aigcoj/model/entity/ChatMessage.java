package com.jay.aigcoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 好友私聊消息
 */
@TableName("chat_message")
@Data
public class ChatMessage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long fromId;

    private Long toId;

    private String content;

    /**
     * 0 未读 / 1 已读
     */
    private Integer isRead;

    private Date createTime;

    private static final long serialVersionUID = 1L;
}
