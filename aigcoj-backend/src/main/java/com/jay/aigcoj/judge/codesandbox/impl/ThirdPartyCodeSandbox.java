package com.jay.aigcoj.judge.codesandbox.impl;

import com.jay.aigcoj.judge.codesandbox.CodeSandbox;
import com.jay.aigcoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.jay.aigcoj.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 第三方代码沙箱（调用网上现成的代码沙箱）
 */
public class ThirdPartyCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("第三方代码沙箱");
        return null;
    }
}
