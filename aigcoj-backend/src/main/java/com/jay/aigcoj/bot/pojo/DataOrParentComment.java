package com.jay.aigcoj.bot.pojo;

import lombok.Data;

import java.util.List;

@Data
public class DataOrParentComment {
    private String id;

    private String questionId;

    private String userId;

    private String userName;

    private String userAvatar;

    private String content;

    private String parentId;

    private int commentNum;

    private int likeCount;

    private boolean isLike;

    private List<String> likeListId;

    private boolean inputShow;

    private String fromId;

    private String fromName;

    private String gmtModified;

    private String gmtCreate;

    private List<QReply> reply;
}
