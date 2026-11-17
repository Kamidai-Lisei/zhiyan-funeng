package com.jay.aigcoj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jay.aigcoj.common.BaseResponse;
import com.jay.aigcoj.common.ErrorCode;
import com.jay.aigcoj.common.ResultUtils;
import com.jay.aigcoj.exception.BusinessException;
import com.jay.aigcoj.exception.ThrowUtils;
import com.jay.aigcoj.mapper.PostCommentMapper;
import com.jay.aigcoj.model.entity.PostComment;
import com.jay.aigcoj.model.entity.User;
import com.jay.aigcoj.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子评论
 */
@RestController
@RequestMapping("/post_comment")
public class PostCommentController {

    @Resource
    private PostCommentMapper commentMapper;

    @Resource
    private UserService userService;

    @GetMapping("/list")
    public BaseResponse<List<Map<String, Object>>> list(@RequestParam long postId) {
        List<PostComment> comments = commentMapper.selectList(new QueryWrapper<PostComment>()
                .eq("postId", postId).orderByAsc("id"));
        if (comments.isEmpty()) {
            return ResultUtils.success(Collections.emptyList());
        }
        Set<Long> userIds = comments.stream().map(PostComment::getUserId).collect(Collectors.toSet());
        Map<Long, User> users = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        List<Map<String, Object>> result = new ArrayList<>();
        for (PostComment comment : comments) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", comment.getId());
            item.put("postId", comment.getPostId());
            item.put("content", comment.getContent());
            item.put("parentId", comment.getParentId());
            item.put("createTime", comment.getCreateTime());
            User user = users.get(comment.getUserId());
            item.put("user", userService.getUserVO(user));
            result.add(item);
        }
        return ResultUtils.success(result);
    }

    @PostMapping("/add")
    public BaseResponse<Long> add(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        Long postId = body.get("postId") == null ? null : Long.valueOf(String.valueOf(body.get("postId")));
        String content = StringUtils.trimToEmpty(String.valueOf(body.get("content")));
        ThrowUtils.throwIf(postId == null || content.isEmpty(), ErrorCode.PARAMS_ERROR, "评论内容不能为空");
        ThrowUtils.throwIf(content.length() > 2000, ErrorCode.PARAMS_ERROR, "评论过长");
        PostComment comment = new PostComment();
        comment.setPostId(postId);
        comment.setUserId(user.getId());
        comment.setContent(content);
        comment.setParentId(body.get("parentId") == null ? 0L : Long.valueOf(String.valueOf(body.get("parentId"))));
        commentMapper.insert(comment);
        return ResultUtils.success(comment.getId());
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> delete(@RequestBody Map<String, Long> body, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        Long id = body.get("id");
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        PostComment comment = commentMapper.selectById(id);
        ThrowUtils.throwIf(comment == null, ErrorCode.NOT_FOUND_ERROR);
        if (!comment.getUserId().equals(user.getId()) && !"admin".equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return ResultUtils.success(commentMapper.deleteById(id) > 0);
    }

    @GetMapping("/count")
    public BaseResponse<Long> count(@RequestParam long postId) {
        return ResultUtils.success(commentMapper.selectCount(new QueryWrapper<PostComment>().eq("postId", postId)));
    }
}
