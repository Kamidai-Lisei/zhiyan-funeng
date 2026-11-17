package com.jay.aigcoj.judge;

import cn.hutool.json.JSONUtil;
import com.jay.aigcoj.common.ErrorCode;
import com.jay.aigcoj.exception.BusinessException;
import com.jay.aigcoj.judge.codesandbox.CodeSandbox;
import com.jay.aigcoj.judge.codesandbox.CodeSandboxFactory;
import com.jay.aigcoj.judge.codesandbox.CodeSandboxProxy;
import com.jay.aigcoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.jay.aigcoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.jay.aigcoj.judge.strategy.JudgeContext;
import com.jay.aigcoj.model.dto.question.JudgeCase;
import com.jay.aigcoj.judge.codesandbox.model.JudgeInfo;
import com.jay.aigcoj.model.entity.Question;
import com.jay.aigcoj.model.entity.QuestionSubmit;
import com.jay.aigcoj.model.enums.JudgeInfoMessageEnum;
import com.jay.aigcoj.model.enums.QuestionSubmitStatusEnum;
import org.apache.commons.lang3.StringUtils;
import com.jay.aigcoj.service.QuestionService;
import com.jay.aigcoj.service.QuestionSubmitService;
import com.jay.aigcoj.service.SwitchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private JudgeManager judgeManager;

    @Resource
    private SwitchService switchService;

    @Value("${codesandbox.type}")
    private String type;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        // 1）传入题目的提交 id，获取到对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionSubmitService.getById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 2）如果题目提交状态不为等待中，就不用重复执行了
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        // 3）更改判题（题目提交）的状态为 “判题中”，防止重复执行
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        // 4）调用沙箱，获取到执行结果
        //通过数据库判断是否开启了AI判题开关
        long status = switchService.getStatus(1);
        //由于type是一个静态变量，只会在项目启动的时候读取一次
        if (status == 1) type = "ai";
        else if (status == 0) type = "remote";
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        codeSandbox = new CodeSandboxProxy(codeSandbox);
        String language = questionSubmit.getLanguage();
        String code = questionSubmit.getCode();

        // 获取输入用例
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = null;
        try {
            executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        } catch (Exception e) {
            // 如果沙箱服务不可用，捕获异常并将状态设置为 FAILED
            questionSubmitUpdate = new QuestionSubmit();
            questionSubmitUpdate.setId(questionSubmitId);
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
            boolean updateFailed = questionSubmitService.updateById(questionSubmitUpdate);
            if (!updateFailed) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "沙箱服务不可用: " + e.getMessage());
        }

        // 5）根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(executeCodeResponse.getOutputList());
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);

        // 编译/运行错误时，把沙箱捕获的编译器输出或异常信息透出给用户，避免只看到干巴巴的错误名
        String judgedMessage = judgeInfo.getMessage();
        if ((JudgeInfoMessageEnum.COMPILE_ERROR.getValue().equals(judgedMessage)
                || JudgeInfoMessageEnum.RUNTIME_ERROR.getValue().equals(judgedMessage))
                && StringUtils.isBlank(judgeInfo.getDetail())) {
            String sandboxDetail = executeCodeResponse.getMessage();
            if (StringUtils.isNotBlank(sandboxDetail)) {
                judgeInfo.setDetail(sandboxDetail.length() > 800 ? sandboxDetail.substring(0, 800) : sandboxDetail);
            }
        }

        //更新题目提交数和通过数
        Question questionUpdate = new Question();
        questionUpdate.setId(questionId);
        questionUpdate.setSubmitNum(question.getSubmitNum() + 1);
        if ("Accepted".equals(judgeInfo.getMessage())){
            questionUpdate.setAcceptedNum(question.getAcceptedNum() + 1);
        }
        questionService.updateById(questionUpdate);

        // 6）修改数据库中的判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        update = questionSubmitService.updateById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目状态更新错误");
        }
        QuestionSubmit questionSubmitResult = questionSubmitService.getById(questionSubmitId);
        return questionSubmitResult;
    }
}
