package com.jay.aigcoj.bot.pojo;

import lombok.Data;

import java.util.List;

@Data
public class QReply {
    private String id;

    private String questionId;

    private String userId;

    private String userName;

    private String userAvatar;

    private String content;

    private String parentId;

    private String commentNum;

    private int likeCount;

    private boolean isLike;

    private List<String> likeListId;

    private String inputShow;

    private String fromId;

    private String fromName;

    private String gmtModified;

    private String gmtCreate;

    private String reply;

}
