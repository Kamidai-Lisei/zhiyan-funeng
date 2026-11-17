package com.jay.aigcoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.jay.aigcoj.mapper.SwitchMapper;
import com.jay.aigcoj.model.entity.Switch;
import com.jay.aigcoj.service.SwitchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
@Slf4j
public class SwitchServiceImpl extends ServiceImpl<SwitchMapper, Switch> implements SwitchService {

    @Resource
    private SwitchMapper switchMapper;
    @Override
    public boolean changeStatus(long id) {
        long flag = this.getStatus(id);
        UpdateWrapper<Switch> updateWrapper = new UpdateWrapper<>();
        if (flag == 1){
            updateWrapper.eq("id", id).set("flag",0);
        }else if (flag == 0){
            updateWrapper.eq("id", id).set("flag",1);
        }
        return switchMapper.update(null, updateWrapper)>0;
    }
    @Override
    public long getStatus(long id) {
        QueryWrapper<Switch> queryWrapper =new QueryWrapper<>();
        queryWrapper.eq("id",id);
        Switch aSwitch = this.baseMapper.selectOne(queryWrapper);
        return aSwitch.getFlag();
    }

}
