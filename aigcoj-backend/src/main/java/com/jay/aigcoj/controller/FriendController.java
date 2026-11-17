package com.jay.aigcoj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jay.aigcoj.common.BaseResponse;
import com.jay.aigcoj.common.ErrorCode;
import com.jay.aigcoj.common.ResultUtils;
import com.jay.aigcoj.exception.ThrowUtils;
import com.jay.aigcoj.mapper.ChatMessageMapper;
import com.jay.aigcoj.mapper.FriendLinkMapper;
import com.jay.aigcoj.mapper.UserMapper;
import com.jay.aigcoj.model.entity.ChatMessage;
import com.jay.aigcoj.model.entity.FriendLink;
import com.jay.aigcoj.model.entity.User;
import com.jay.aigcoj.model.vo.UserVO;
import com.jay.aigcoj.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 好友与私聊：加好友、同意/拒绝、好友列表、消息收发（轮询拉取）
 */
@RestController
@RequestMapping("/friend")
public class FriendController {

    @Resource
    private FriendLinkMapper friendLinkMapper;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    /**
     * 好友列表（含未读数与最后一条消息）
     */
    @GetMapping("/list")
    public BaseResponse<List<Map<String, Object>>> list(HttpServletRequest request) {
        User me = userService.getLoginUser(request);
        List<FriendLink> links = friendLinkMapper.selectList(new QueryWrapper<FriendLink>()
                .eq("status", 1)
                .and(w -> w.eq("userId", me.getId()).or().eq("friendId", me.getId())));
        List<Map<String, Object>> result = new ArrayList<>();
        for (FriendLink link : links) {
            Long otherId = link.getUserId().equals(me.getId()) ? link.getFriendId() : link.getUserId();
            User other = userMapper.selectById(otherId);
            if (other == null) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", other.getId());
            m.put("userName", other.getUserName());
            m.put("userAccount", other.getUserAccount());
            m.put("userAvatar", other.getUserAvatar());
            ChatMessage last = chatMessageMapper.selectOne(new QueryWrapper<ChatMessage>()
                    .and(w -> w.and(q -> q.eq("fromId", me.getId()).eq("toId", otherId))
                            .or(q -> q.eq("fromId", otherId).eq("toId", me.getId())))
                    .orderByDesc("id").last("limit 1"));
            m.put("lastMessage", last == null ? "" : last.getContent());
            m.put("lastTime", last == null ? null : last.getCreateTime());
            Long unread = chatMessageMapper.selectCount(new QueryWrapper<ChatMessage>()
                    .eq("fromId", otherId).eq("toId", me.getId()).eq("isRead", 0));
            m.put("unread", unread == null ? 0 : unread);
            result.add(m);
        }
        result.sort((a, b) -> {
            Date da = (Date) a.get("lastTime");
            Date db = (Date) b.get("lastTime");
            return db == null ? -1 : (da == null ? 1 : db.compareTo(da));
        });
        return ResultUtils.success(result);
    }

    /**
     * 收到的好友申请列表
     */
    @GetMapping("/requests")
    public BaseResponse<List<Map<String, Object>>> requests(HttpServletRequest request) {
        User me = userService.getLoginUser(request);
        List<FriendLink> pending = friendLinkMapper.selectList(new QueryWrapper<FriendLink>()
                .eq("friendId", me.getId()).eq("status", 0).orderByDesc("id"));
        List<Map<String, Object>> result = pending.stream().map(l -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("requestId", l.getId());
            m.put("fromId", l.getUserId());
            m.put("createTime", l.getCreateTime());
            User u = userMapper.selectById(l.getUserId());
            if (u != null) {
                m.put("userName", u.getUserName());
                m.put("userAvatar", u.getUserAvatar());
                m.put("userProfile", u.getUserProfile());
            }
            return m;
        }).collect(Collectors.toList());
        return ResultUtils.success(result);
    }

    /**
     * 发送好友申请
     */
    @PostMapping("/request")
    public BaseResponse<Boolean> addFriend(@RequestBody Map<String, Long> body, HttpServletRequest request) {
        User me = userService.getLoginUser(request);
        Long friendId = body.get("friendId");
        ThrowUtils.throwIf(friendId == null || friendId.equals(me.getId()), ErrorCode.PARAMS_ERROR, "无效的好友目标");
        User target = userMapper.selectById(friendId);
        ThrowUtils.throwIf(target == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        FriendLink existed = findLink(me.getId(), friendId);
        if (existed != null) {
            if (existed.getStatus() == 1) {
                return ResultUtils.success(true); // 已是好友
            }
            // 对方也曾向我发起申请：直接互相通过
            if (existed.getUserId().equals(friendId)) {
                existed.setStatus(1);
                friendLinkMapper.updateById(existed);
                return ResultUtils.success(true);
            }
            existed.setUpdateTime(new Date());
            friendLinkMapper.updateById(existed);
            return ResultUtils.success(true);
        }
        FriendLink link = new FriendLink();
        link.setUserId(me.getId());
        link.setFriendId(friendId);
        link.setStatus(0);
        friendLinkMapper.insert(link);
        return ResultUtils.success(true);
    }

    /**
     * 同意好友申请
     */
    @PostMapping("/accept")
    public BaseResponse<Boolean> accept(@RequestBody Map<String, Long> body, HttpServletRequest request) {
        User me = userService.getLoginUser(request);
        Long requestId = body.get("requestId");
        FriendLink link = friendLinkMapper.selectById(requestId);
        ThrowUtils.throwIf(link == null || !link.getFriendId().equals(me.getId()),
                ErrorCode.NO_AUTH_ERROR, "无权处理该申请");
        link.setStatus(1);
        link.setUpdateTime(new Date());
        friendLinkMapper.updateById(link);
        return ResultUtils.success(true);
    }

    /**
     * 拒绝/删除好友关系（无论方向）
     */
    @PostMapping("/remove")
    public BaseResponse<Boolean> remove(@RequestBody Map<String, Long> body, HttpServletRequest request) {
        User me = userService.getLoginUser(request);
        Long friendId = body.get("friendId") != null ? body.get("friendId") : body.get("requestId");
        ThrowUtils.throwIf(friendId == null, ErrorCode.PARAMS_ERROR);
        FriendLink link = findLink(me.getId(), friendId);
        if (link != null) {
            friendLinkMapper.deleteById(link.getId());
        }
        return ResultUtils.success(true);
    }

    /**
     * 搜索可添加的用户（按用户名/账号模糊）
     */
    @GetMapping("/search")
    public BaseResponse<List<Map<String, Object>>> search(@RequestParam String name, HttpServletRequest request) {
        User me = userService.getLoginUser(request);
        ThrowUtils.throwIf(StringUtils.isBlank(name), ErrorCode.PARAMS_ERROR, "请输入用户名或账号");
        List<User> users = userMapper.selectList(new QueryWrapper<User>()
                .ne("id", me.getId())
                .and(w -> w.like("userName", name).or().like("userAccount", name))
                .last("limit 20"));
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("userName", u.getUserName());
            m.put("userAccount", u.getUserAccount());
            m.put("userAvatar", u.getUserAvatar());
            m.put("userProfile", u.getUserProfile());
            FriendLink link = findLink(me.getId(), u.getId());
            m.put("status", link == null ? "none" : (link.getStatus() == 1 ? "friends" :
                    (link.getUserId().equals(me.getId()) ? "sent" : "received")));
            return m;
        }).collect(Collectors.toList());
        return ResultUtils.success(result);
    }

    /**
     * 发送私聊消息
     */
    @PostMapping("/send")
    public BaseResponse<Boolean> send(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User me = userService.getLoginUser(request);
        Long toId = Long.valueOf(String.valueOf(body.get("toId")));
        String content = StringUtils.trimToEmpty(String.valueOf(body.get("content")));
        ThrowUtils.throwIf(StringUtils.isEmpty(content), ErrorCode.PARAMS_ERROR, "消息不能为空");
        ThrowUtils.throwIf(content.length() > 2000, ErrorCode.PARAMS_ERROR, "消息过长");
        FriendLink link = findLink(me.getId(), toId);
        ThrowUtils.throwIf(link == null || link.getStatus() != 1, ErrorCode.NO_AUTH_ERROR, "仅好友之间可以私聊");
        ChatMessage msg = new ChatMessage();
        msg.setFromId(me.getId());
        msg.setToId(toId);
        msg.setContent(content);
        msg.setIsRead(0);
        chatMessageMapper.insert(msg);
        return ResultUtils.success(true);
    }

    /**
     * 拉取与某好友的聊天记录（afterId 之后的增量，同时把收到的标为已读）
     */
    @GetMapping("/messages")
    public BaseResponse<Map<String, Object>> messages(@RequestParam long friendId,
                                                      @RequestParam(required = false, defaultValue = "0") long afterId,
                                                      HttpServletRequest request) {
        User me = userService.getLoginUser(request);
        List<ChatMessage> list = chatMessageMapper.selectList(new QueryWrapper<ChatMessage>()
                .and(w -> w.and(q -> q.eq("fromId", me.getId()).eq("toId", friendId))
                        .or(q -> q.eq("fromId", friendId).eq("toId", me.getId())))
                .gt("id", afterId)
                .orderByAsc("id").last("limit 200"));
        // 标记对方发给我的为已读
        chatMessageMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<ChatMessage>()
                .eq("fromId", friendId).eq("toId", me.getId()).eq("isRead", 0).set("isRead", 1));
        List<Map<String, Object>> msgs = list.stream().map(m -> {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id", m.getId());
            r.put("fromId", m.getFromId());
            r.put("toId", m.getToId());
            r.put("content", m.getContent());
            r.put("mine", m.getFromId().equals(me.getId()));
            r.put("createTime", m.getCreateTime());
            return r;
        }).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("messages", msgs);
        User other = userMapper.selectById(friendId);
        if (other != null) {
            result.put("friend", userService.getUserVO(other));
        }
        return ResultUtils.success(result);
    }

    private FriendLink findLink(Long a, Long b) {
        FriendLink link = friendLinkMapper.selectOne(new QueryWrapper<FriendLink>()
                .eq("userId", a).eq("friendId", b));
        if (link != null) {
            return link;
        }
        return friendLinkMapper.selectOne(new QueryWrapper<FriendLink>()
                .eq("userId", b).eq("friendId", a));
    }
}
