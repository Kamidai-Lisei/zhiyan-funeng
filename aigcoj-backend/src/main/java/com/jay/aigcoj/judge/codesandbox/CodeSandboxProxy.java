package com.jay.aigcoj.judge.codesandbox;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.jay.aigcoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.jay.aigcoj.judge.codesandbox.model.ExecuteCodeResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CodeSandboxProxy implements CodeSandbox {

    private final CodeSandbox codeSandbox;


    public CodeSandboxProxy(CodeSandbox codeSandbox) {
        this.codeSandbox = codeSandbox;
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        log.info("代码沙箱请求信息：" + executeCodeRequest.toString());
        ExecuteCodeResponse executeCodeResponse = null;
        try {
            executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }
        log.info("代码沙箱响应信息：" + executeCodeResponse.toString());
        return executeCodeResponse;
    }
}
