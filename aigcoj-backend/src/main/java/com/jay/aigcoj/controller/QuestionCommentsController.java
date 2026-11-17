package com.jay.aigcoj.controller;

import com.jay.aigcoj.common.BaseResponse;
import com.jay.aigcoj.common.ErrorCode;
import com.jay.aigcoj.common.ResultUtils;
import com.jay.aigcoj.exception.BusinessException;
import com.jay.aigcoj.model.dto.questioncomment.CommentAddRequest;
import com.jay.aigcoj.model.entity.QuestionComment;
import com.jay.aigcoj.model.entity.User;
import com.jay.aigcoj.model.vo.QuestionCommentVO;
import com.jay.aigcoj.service.QuestionCommentService;
import com.jay.aigcoj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 帖子接口
 * 
 */
@RestController
@RequestMapping("/question_comment")
@Slf4j
public class QuestionCommentsController {


    @Resource
    private UserService userService;

    @Resource
    private QuestionCommentService questionCommentService;

    // region 增删改查

    /**
     * 获取该问题的所有评论
     *
     * @param id
     * @return
     */
    @GetMapping("/getCommentList")
    public BaseResponse<List<QuestionCommentVO>> getCommentList(long id) {
        return ResultUtils.success(questionCommentService.getAllCommentList(id));
    }


    @PostMapping("/addComment")
    public BaseResponse<Boolean> addQuestionComment(@RequestBody QuestionComment currentComment, @RequestBody(required = false) QuestionComment parent, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        boolean b = questionCommentService.addComment(currentComment, parent, loginUser);
        return ResultUtils.success(b);
    }

    @PostMapping("wrap/addComment")
    public BaseResponse<Boolean> addQuestionCommentWrap(@RequestBody CommentAddRequest commentAddRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        QuestionComment currentComment = commentAddRequest.getCurrentComment();
        QuestionComment parent = commentAddRequest.getParentComment();
        boolean b = questionCommentService.addComment(currentComment, parent, loginUser);
        return ResultUtils.success(b);
    }


    /**
     * 删除
     *
     * @param currentComment
     * @return
     */
    @PostMapping("/deleteComment")
    public BaseResponse<Integer> deleteQuestion(@RequestBody QuestionComment currentComment, HttpServletRequest request) {
        if (currentComment == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能删除空评论");
        }
        User loginUser = userService.getLoginUser(request);
        int updateCount = questionCommentService.deleteCommentById(currentComment, loginUser);
        return ResultUtils.success(updateCount);

    }

    /**
     * 更新（仅管理员）
     *
     * @param currentComment
     * @return
     */
    @PostMapping("/updateLikeCount")
    public BaseResponse<Boolean> updateQuestionComment(@RequestBody QuestionComment currentComment) {
        boolean updateLikeCount = questionCommentService.updateLikeCount(currentComment);
        return ResultUtils.success(updateLikeCount);
    }


}
