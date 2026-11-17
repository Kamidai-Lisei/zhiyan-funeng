package com.jay.aigcoj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jay.aigcoj.common.BaseResponse;
import com.jay.aigcoj.common.ErrorCode;
import com.jay.aigcoj.common.ResultUtils;
import com.jay.aigcoj.exception.BusinessException;
import com.jay.aigcoj.exception.ThrowUtils;
import com.jay.aigcoj.mapper.MaterialFavourMapper;
import com.jay.aigcoj.mapper.StudyMaterialMapper;
import com.jay.aigcoj.model.entity.MaterialFavour;
import com.jay.aigcoj.model.entity.StudyMaterial;
import com.jay.aigcoj.model.entity.User;
import com.jay.aigcoj.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考研资料库
 */
@RestController
@RequestMapping("/material")
public class MaterialController {

    @Resource
    private StudyMaterialMapper materialMapper;

    @Resource
    private MaterialFavourMapper favourMapper;

    @Resource
    private UserService userService;

    @GetMapping("/list/page")
    public BaseResponse<Page<StudyMaterial>> listPage(@RequestParam(defaultValue = "1") long current,
                                                      @RequestParam(defaultValue = "12") long pageSize,
                                                      @RequestParam(required = false) String category,
                                                      @RequestParam(required = false) String subject,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(defaultValue = "new") String sort) {
        Page<StudyMaterial> page = new Page<>(current, Math.min(pageSize, 60));
        QueryWrapper<StudyMaterial> qw = new QueryWrapper<>();
        qw.eq("status", 1);
        qw.eq(StringUtils.isNotBlank(category), "category", category);
        qw.eq(StringUtils.isNotBlank(subject), "subject", subject);
        if (StringUtils.isNotBlank(keyword)) {
            qw.and(w -> w.like("title", keyword).or().like("description", keyword).or().like("tags", keyword));
        }
        if ("hot".equals(sort)) {
            qw.orderByDesc("downloadNum", "viewNum");
        } else if ("favour".equals(sort)) {
            qw.orderByDesc("favourNum");
        } else {
            qw.orderByDesc("createTime");
        }
        materialMapper.selectPage(page, qw);
        return ResultUtils.success(page);
    }

    @GetMapping("/get")
    public BaseResponse<Map<String, Object>> get(@RequestParam long id, HttpServletRequest request) {
        StudyMaterial material = materialMapper.selectById(id);
        ThrowUtils.throwIf(material == null, ErrorCode.NOT_FOUND_ERROR, "资料不存在");
        materialMapper.update(null, new UpdateWrapper<StudyMaterial>()
                .eq("id", id).setSql("viewNum = viewNum + 1"));
        Map<String, Object> result = new HashMap<>();
        result.put("material", material);
        User user = userService.getLoginUserPermitNull(request);
        boolean hasFavour = false;
        if (user != null) {
            hasFavour = favourMapper.selectCount(new QueryWrapper<MaterialFavour>()
                    .eq("materialId", id).eq("userId", user.getId())) > 0;
        }
        result.put("hasFavour", hasFavour);
        return ResultUtils.success(result);
    }

    @GetMapping("/categories")
    public BaseResponse<List<Map<String, Object>>> categories() {
        QueryWrapper<StudyMaterial> qw = new QueryWrapper<>();
        qw.select("category as name", "count(*) as count").eq("status", 1).groupBy("category");
        return ResultUtils.success(materialMapper.selectMaps(qw));
    }

    @PostMapping("/save")
    public BaseResponse<Long> save(@RequestBody StudyMaterial material, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(material == null || StringUtils.isBlank(material.getTitle()), ErrorCode.PARAMS_ERROR, "标题不能为空");
        if (!"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        if (material.getId() == null) {
            material.setUploaderId(loginUser.getId());
            material.setDownloadNum(0);
            material.setFavourNum(0);
            material.setViewNum(0);
            material.setStatus(1);
            materialMapper.insert(material);
        } else {
            materialMapper.updateById(material);
        }
        return ResultUtils.success(material.getId());
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> delete(@RequestBody Map<String, Long> body, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (!"admin".equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Long id = body.get("id");
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(materialMapper.deleteById(id) > 0);
    }

    @PostMapping("/favour")
    public BaseResponse<Map<String, Object>> favour(@RequestBody Map<String, Long> body, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long materialId = body.get("materialId");
        ThrowUtils.throwIf(materialId == null, ErrorCode.PARAMS_ERROR);
        MaterialFavour existed = favourMapper.selectOne(new QueryWrapper<MaterialFavour>()
                .eq("materialId", materialId).eq("userId", loginUser.getId()).last("limit 1"));
        boolean favour;
        if (existed != null) {
            favourMapper.deleteById(existed.getId());
            materialMapper.update(null, new UpdateWrapper<StudyMaterial>()
                    .eq("id", materialId).setSql("favourNum = GREATEST(favourNum - 1, 0)"));
            favour = false;
        } else {
            MaterialFavour record = new MaterialFavour();
            record.setMaterialId(materialId);
            record.setUserId(loginUser.getId());
            favourMapper.insert(record);
            materialMapper.update(null, new UpdateWrapper<StudyMaterial>()
                    .eq("id", materialId).setSql("favourNum = favourNum + 1"));
            favour = true;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("favour", favour);
        return ResultUtils.success(result);
    }

    @GetMapping("/favour/ids")
    public BaseResponse<List<Long>> favourIds(HttpServletRequest request) {
        User user = userService.getLoginUserPermitNull(request);
        if (user == null) {
            return ResultUtils.success(Collections.emptyList());
        }
        List<MaterialFavour> list = favourMapper.selectList(new QueryWrapper<MaterialFavour>()
                .eq("userId", user.getId()));
        return ResultUtils.success(list.stream().map(MaterialFavour::getMaterialId).collect(Collectors.toList()));
    }

    @GetMapping("/my/favour")
    public BaseResponse<List<StudyMaterial>> myFavour(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        List<MaterialFavour> list = favourMapper.selectList(new QueryWrapper<MaterialFavour>()
                .eq("userId", user.getId()).orderByDesc("id"));
        if (list.isEmpty()) {
            return ResultUtils.success(Collections.emptyList());
        }
        List<Long> ids = list.stream().map(MaterialFavour::getMaterialId).collect(Collectors.toList());
        return ResultUtils.success(materialMapper.selectBatchIds(ids));
    }

    @GetMapping("/download")
    public BaseResponse<Map<String, Object>> download(@RequestParam long id) {
        StudyMaterial material = materialMapper.selectById(id);
        ThrowUtils.throwIf(material == null, ErrorCode.NOT_FOUND_ERROR, "资料不存在");
        materialMapper.update(null, new UpdateWrapper<StudyMaterial>()
                .eq("id", id).setSql("downloadNum = downloadNum + 1"));
        Map<String, Object> result = new HashMap<>();
        result.put("url", material.getFileUrl());
        result.put("title", material.getTitle());
        return ResultUtils.success(result);
    }

    @GetMapping("/stats")
    public BaseResponse<Map<String, Object>> stats() {
        Map<String, Object> map = new HashMap<>();
        map.put("total", materialMapper.selectCount(new QueryWrapper<StudyMaterial>().eq("status", 1)));
        QueryWrapper<StudyMaterial> qw = new QueryWrapper<>();
        qw.select("sum(downloadNum) as downloads", "sum(viewNum) as views");
        List<Map<String, Object>> rows = materialMapper.selectMaps(qw);
        map.put("downloads", rows.isEmpty() ? 0 : rows.get(0).get("downloads"));
        map.put("views", rows.isEmpty() ? 0 : rows.get(0).get("views"));
        return ResultUtils.success(map);
    }
}
