package com.jay.aigcoj.bot.pojo;

import lombok.Data;

@Data
public class ARoot {

    private ACurrentComment currentComment;

    private DataOrParentComment dataOrParentComment;

    public ARoot(ACurrentComment currentComment, DataOrParentComment dataOrParentComment) {
        this.currentComment = currentComment;
        this.dataOrParentComment = dataOrParentComment;
    }
}
