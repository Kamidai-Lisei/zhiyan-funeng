package com.jay.aigcoj.judge;

import com.jay.aigcoj.judge.strategy.DefaultJudgeStrategy;
import com.jay.aigcoj.judge.strategy.JavaLanguageJudgeStrategy;
import com.jay.aigcoj.judge.strategy.JudgeContext;
import com.jay.aigcoj.judge.strategy.JudgeStrategy;
import com.jay.aigcoj.judge.codesandbox.model.JudgeInfo;
import com.jay.aigcoj.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 *
 * 用于判断调用哪种判题策略->judgeStrategy.doJudge(judgeContext)
 */
@Service
public class JudgeManager {

    /**
     * 执行判题
     *
     * @param judgeContext
     * @return
     */
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        QuestionSubmit questionSubmit = judgeContext.getQuestionSubmit();
        String language = questionSubmit.getLanguage();

        //
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)) {
            judgeStrategy = new JavaLanguageJudgeStrategy();
        }
        return judgeStrategy.doJudge(judgeContext);
    }

}
