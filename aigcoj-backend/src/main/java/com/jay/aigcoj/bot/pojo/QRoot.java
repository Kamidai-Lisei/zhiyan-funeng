package com.jay.aigcoj.bot.pojo;

import lombok.Data;

import java.util.List;

@Data
public class QRoot {
    private int code;

    private List<DataOrParentComment> data;

    private String message;

}
