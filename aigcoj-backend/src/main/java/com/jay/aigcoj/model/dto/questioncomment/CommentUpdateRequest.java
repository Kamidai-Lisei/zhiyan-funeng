package com.jay.aigcoj.model.dto.questioncomment;

import com.jay.aigcoj.model.entity.QuestionComment;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 */
@Data
public class CommentUpdateRequest implements Serializable {

    /**
     * 当前评论
     */
    private QuestionComment currentComment;
//    private Long id;
//
//    /**
//     * 点赞数
//     */
//    private Integer likeCount;

}
