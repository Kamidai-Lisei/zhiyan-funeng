package com.jay.aigcoj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jay.aigcoj.model.entity.Switch;


public interface SwitchService extends IService<Switch> {
    boolean changeStatus(long id);
    long getStatus(long id);

}