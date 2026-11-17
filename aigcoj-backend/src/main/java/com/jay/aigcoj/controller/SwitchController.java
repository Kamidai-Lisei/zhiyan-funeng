package com.jay.aigcoj.controller;


import com.jay.aigcoj.common.BaseResponse;
import com.jay.aigcoj.common.ResultUtils;
import com.jay.aigcoj.service.SwitchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;


@RestController
@RequestMapping("/switch")
@Slf4j
public class SwitchController {

    @Resource
    private SwitchService switchService;

    @PostMapping("/change")
    public BaseResponse<Boolean> ChangeStatus(@RequestBody Map<String, Integer> request) {
        long id = request.get("id");
        boolean result = switchService.changeStatus(id);
        return ResultUtils.success(result);
    }
    @GetMapping("/get")
    public BaseResponse<Long> GetStatus(@RequestParam long id) {
        long result = switchService.getStatus(id);
        return ResultUtils.success(result);
    }

}
