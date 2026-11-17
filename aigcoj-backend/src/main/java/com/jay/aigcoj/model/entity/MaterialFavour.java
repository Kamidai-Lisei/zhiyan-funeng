package com.jay.aigcoj.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 资料收藏
 */
@TableName("material_favour")
@Data
public class MaterialFavour implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long materialId;

    private Long userId;

    private Date createTime;

    private static final long serialVersionUID = 1L;
}
