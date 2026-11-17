package com.jay.aigcoj.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName(value = "switch")
@Data
public class Switch {

    private Long id;

    private Integer flag;
}
