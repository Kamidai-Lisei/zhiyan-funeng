package com.jay.aigcoj.bot.pojo;

import lombok.Data;

@Data
public class ACurrentComment {
    private String questionId;

    private String userId;

    private String userName;

    private String userAvatar;

    private String content;

    private String parentId;

    private String likeListId;

    private String fromId;

    private String fromName;

    public ACurrentComment(String questionId, String userId, String userName, String userAvatar, String content, String parentId, String likeListId, String fromId, String fromName) {
        this.questionId = questionId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.content = content;
        this.parentId = parentId;
        this.likeListId = likeListId;
        this.fromId = fromId;
        this.fromName = fromName;
    }
}
