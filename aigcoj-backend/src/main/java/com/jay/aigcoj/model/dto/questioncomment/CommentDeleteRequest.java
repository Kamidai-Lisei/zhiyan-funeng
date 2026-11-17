package com.jay.aigcoj.model.dto.questioncomment;

import com.jay.aigcoj.model.entity.QuestionComment;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 */
@Data
public class CommentDeleteRequest implements Serializable {

    /**
     * 当前评论
     */
    private QuestionComment currentComment;


}
