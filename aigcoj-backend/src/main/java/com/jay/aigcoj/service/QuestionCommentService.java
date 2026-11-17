package com.jay.aigcoj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jay.aigcoj.model.entity.QuestionComment;
import com.jay.aigcoj.model.entity.User;
import com.jay.aigcoj.model.vo.QuestionCommentVO;
import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;

public interface QuestionCommentService extends IService<QuestionComment> {

    List<QuestionCommentVO> getAllCommentList(@Param("questionId") long questionId);

    int deleteCommentById(QuestionComment comment, User loginUser);

    boolean addComment(QuestionComment current,QuestionComment parent,User loginUser);

    boolean updateLikeCount(QuestionComment questionComment);

}
