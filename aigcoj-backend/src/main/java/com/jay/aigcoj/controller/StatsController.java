package com.jay.aigcoj.controller;

import com.jay.aigcoj.common.BaseResponse;
import com.jay.aigcoj.common.ResultUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

/**
 * 全站统计与排行榜
 */
@RestController
@RequestMapping("/stats")
public class StatsController {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/overview")
    public BaseResponse<Map<String, Object>> overview() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("users", count("select count(*) from user where isDelete = 0"));
        map.put("questions", count("select count(*) from question where isDelete = 0"));
        map.put("posts", count("select count(*) from post where isDelete = 0"));
        map.put("materials", count("select count(*) from study_material where isDelete = 0 and status = 1"));
        map.put("submissions", count("select count(*) from question_submit where isDelete = 0"));
        map.put("accepted", count("select count(*) from question_submit where isDelete = 0 and status = 2 and judgeInfo like '%Accepted%'"));
        map.put("todayCheckins", count("select count(*) from study_checkin where checkinDate = curdate()"));
        map.put("comments", count("select count(*) from post_comment where isDelete = 0"));
        return ResultUtils.success(map);
    }

    private long count(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value == null ? 0 : value;
    }

    @GetMapping("/ranking")
    public BaseResponse<List<Map<String, Object>>> ranking() {
        String sql = "select u.id, u.userName, u.userAccount, u.userAvatar, u.userProfile, " +
                "count(s.id) as acceptedNum, sum(case when s.judgeInfo like '%Accepted%' then 1 else 0 end) as acCount " +
                "from user u left join question_submit s on s.userId = u.id and s.status = 2 " +
                "where u.isDelete = 0 group by u.id order by acCount desc, acceptedNum desc limit 10";
        return ResultUtils.success(jdbcTemplate.queryForList(sql));
    }

    @GetMapping("/trend")
    public BaseResponse<Map<String, Object>> trend() {
        List<Map<String, Object>> submissions = jdbcTemplate.queryForList(
                "select date(createTime) as day, count(*) as num from question_submit " +
                        "where createTime >= date_sub(curdate(), interval 6 day) group by date(createTime) order by day");
        List<Map<String, Object>> checkins = jdbcTemplate.queryForList(
                "select checkinDate as day, count(*) as num from study_checkin " +
                        "where checkinDate >= date_sub(curdate(), interval 6 day) group by checkinDate order by day");
        Map<String, Object> result = new HashMap<>();
        result.put("submissions", submissions);
        result.put("checkins", checkins);
        return ResultUtils.success(result);
    }

    @GetMapping("/hot")
    public BaseResponse<Map<String, Object>> hot() {
        Map<String, Object> result = new HashMap<>();
        result.put("questions", jdbcTemplate.queryForList(
                "select id, title, submitNum, acceptedNum, tags from question where isDelete = 0 " +
                        "order by submitNum desc limit 6"));
        result.put("materials", jdbcTemplate.queryForList(
                "select id, title, category, fileType, downloadNum, favourNum from study_material " +
                        "where isDelete = 0 and status = 1 order by downloadNum desc limit 6"));
        result.put("posts", jdbcTemplate.queryForList(
                "select id, title, thumbNum, favourNum, tags, createTime from post where isDelete = 0 " +
                        "order by thumbNum desc, id desc limit 6"));
        return ResultUtils.success(result);
    }
}
