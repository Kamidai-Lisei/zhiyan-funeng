package com.jay.aigcoj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jay.aigcoj.common.BaseResponse;
import com.jay.aigcoj.common.ResultUtils;
import com.jay.aigcoj.mapper.PostMapper;
import com.jay.aigcoj.mapper.QuestionMapper;
import com.jay.aigcoj.mapper.StudyMaterialMapper;
import com.jay.aigcoj.model.entity.Post;
import com.jay.aigcoj.model.entity.Question;
import com.jay.aigcoj.model.entity.StudyMaterial;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局搜索
 */
@RestController
@RequestMapping("/search")
public class SearchController {

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private StudyMaterialMapper materialMapper;

    @Resource
    private PostMapper postMapper;

    @GetMapping
    public BaseResponse<Map<String, Object>> search(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        if (StringUtils.isBlank(keyword)) {
            return ResultUtils.success(result);
        }
        QueryWrapper<Question> qw = new QueryWrapper<>();
        qw.and(w -> w.like("title", keyword).or().like("content", keyword).or().like("tags", keyword))
                .last("limit 6");
        List<Question> questions = questionMapper.selectList(qw);

        QueryWrapper<StudyMaterial> mw = new QueryWrapper<>();
        mw.eq("status", 1)
                .and(w -> w.like("title", keyword).or().like("description", keyword).or().like("tags", keyword))
                .last("limit 6");
        List<StudyMaterial> materials = materialMapper.selectList(mw);

        QueryWrapper<Post> pw = new QueryWrapper<>();
        pw.and(w -> w.like("title", keyword).or().like("content", keyword).or().like("tags", keyword))
                .last("limit 6");
        List<Post> posts = postMapper.selectList(pw);

        result.put("questions", questions);
        result.put("materials", materials);
        result.put("posts", posts);
        return ResultUtils.success(result);
    }
}
