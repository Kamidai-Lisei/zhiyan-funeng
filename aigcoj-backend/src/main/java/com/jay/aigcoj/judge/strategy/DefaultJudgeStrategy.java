package com.jay.aigcoj.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.jay.aigcoj.model.dto.question.JudgeCase;
import com.jay.aigcoj.model.dto.question.JudgeConfig;
import com.jay.aigcoj.judge.codesandbox.model.JudgeInfo;
import com.jay.aigcoj.model.entity.Question;
import com.jay.aigcoj.model.enums.JudgeInfoMessageEnum;

import java.util.List;
import java.util.Optional;

/**
 * 默认判题策略：逐用例比对，按通过比例计分（0-100）
 */
public class DefaultJudgeStrategy implements JudgeStrategy {

    /**
     * 时间限制额外宽限（ms），Java 子类放宽以覆盖 JVM 启动开销
     */
    protected long timeSlackMs() {
        return 1000L;
    }

    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        Long memory = Optional.ofNullable(judgeInfo.getMemory()).orElse(0L);
        Long time = Optional.ofNullable(judgeInfo.getTime()).orElse(0L);
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        Question question = judgeContext.getQuestion();
        List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();

        JudgeInfo response = new JudgeInfo();
        response.setMemory(memory);
        response.setTime(time);

        int total = judgeCaseList == null ? 0 : judgeCaseList.size();
        response.setTotal(total);

        String sandboxMessage = Optional.ofNullable(judgeInfo.getMessage()).orElse("");

        // 编译错误：没有任何运行结果，直接 0 分
        if (sandboxMessage.contains("Compile Error")) {
            response.setMessage(JudgeInfoMessageEnum.COMPILE_ERROR.getValue());
            fillScore(response, 0, total);
            return response;
        }

        // 逐用例比对，统计通过数并记录首个失败用例
        int passed = 0;
        StringBuilder detail = null;
        boolean comparable = outputList != null && judgeCaseList != null && outputList.size() == inputList.size();
        if (comparable) {
            for (int i = 0; i < judgeCaseList.size(); i++) {
                String expected = normalize(judgeCaseList.get(i).getOutput());
                String actual = normalize(outputList.get(i));
                if (expected.equals(actual)) {
                    passed++;
                } else if (detail == null) {
                    detail = new StringBuilder()
                            .append("第 ").append(i + 1).append(" 个用例未通过\n")
                            .append("输入：").append(limit(judgeCaseList.get(i).getInput())).append("\n")
                            .append("期望输出：").append(limit(expected)).append("\n")
                            .append("实际输出：").append(actual.isEmpty() ? "（无输出）" : limit(actual));
                }
            }
        }
        fillScore(response, passed, total);
        if (detail != null) {
            response.setDetail(detail.toString());
        }

        // 运行期错误 / 超时：保留已跑出的部分分，但结论以错误为准
        if (sandboxMessage.contains("Runtime Error")) {
            response.setMessage(JudgeInfoMessageEnum.RUNTIME_ERROR.getValue());
            return response;
        }
        if (sandboxMessage.contains("Time Limit")) {
            response.setMessage(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getValue());
            return response;
        }
        if (!comparable) {
            response.setMessage(JudgeInfoMessageEnum.WRONG_ANSWER.getValue());
            return response;
        }
        if (passed < total) {
            response.setMessage(JudgeInfoMessageEnum.WRONG_ANSWER.getValue());
            return response;
        }

        JudgeConfig judgeConfig = JSONUtil.toBean(question.getJudgeConfig(), JudgeConfig.class);
        Long memoryLimit = judgeConfig.getMemoryLimit();
        Long timeLimit = judgeConfig.getTimeLimit();
        // 配置中的时间限制单位为秒
        if (memoryLimit != null && memoryLimit > 0 && memory > memoryLimit) {
            response.setMessage(JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED.getValue());
            return response;
        }
        if (timeLimit != null && timeLimit > 0 && time > timeLimit * 1000 + timeSlackMs()) {
            response.setMessage(JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED.getValue());
            return response;
        }
        response.setMessage(JudgeInfoMessageEnum.ACCEPTED.getValue());
        return response;
    }

    private void fillScore(JudgeInfo response, int passed, int total) {
        response.setPassed(passed);
        response.setScore(total <= 0 ? 0 : Math.round(passed * 100f / total));
    }

    private String limit(String text) {
        if (text == null) {
            return "";
        }
        String oneLine = text.replace("\n", " ⏎ ");
        return oneLine.length() > 120 ? oneLine.substring(0, 120) + "…" : oneLine;
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\r\n", "\n").trim();
    }
}
