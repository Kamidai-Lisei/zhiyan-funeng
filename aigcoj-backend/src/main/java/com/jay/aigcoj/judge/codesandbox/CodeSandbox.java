package com.jay.aigcoj.judge.codesandbox;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.jay.aigcoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.jay.aigcoj.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 代码沙箱接口定义
 */
public interface CodeSandbox {

    /**
     * 执行代码
     *
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) throws UnirestException;
}
