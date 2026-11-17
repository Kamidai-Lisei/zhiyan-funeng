package com.jay.aigcoj.judge.strategy;

/**
 * Java 程序的判题策略：与默认策略一致，仅放宽时间限制以覆盖 JVM 启动开销
 */
public class JavaLanguageJudgeStrategy extends DefaultJudgeStrategy {

    @Override
    protected long timeSlackMs() {
        return 1500L;
    }
}
