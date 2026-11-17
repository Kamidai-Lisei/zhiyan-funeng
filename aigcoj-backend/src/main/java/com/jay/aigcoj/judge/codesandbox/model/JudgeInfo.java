package com.jay.aigcoj.judge.codesandbox.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 判题信息
 */
@Data
@NoArgsConstructor
public class JudgeInfo {

    /**
     * 程序执行信息（Accepted、Wrong Answer 等）
     */
    private String message;

    /**
     * 消耗内存（KB）
     */
    private Long memory;

    /**
     * 消耗时间（ms）
     */
    private Long time;

    /**
     * 得分（0-100，按通过用例比例）
     */
    private Integer score;

    /**
     * 通过用例数
     */
    private Integer passed;

    /**
     * 用例总数
     */
    private Integer total;

    /**
     * 首个未通过用例的详情（输入 / 期望 / 实际）
     */
    private String detail;

    public JudgeInfo(String message, Long memory, Long time) {
        this.message = message;
        this.memory = memory;
        this.time = time;
    }
}
