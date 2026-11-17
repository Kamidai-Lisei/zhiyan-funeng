package com.jay.aigcoj.judge.codesandbox;

import com.jay.aigcoj.judge.codesandbox.impl.AICodeSandbox;
import com.jay.aigcoj.judge.codesandbox.impl.ExampleCodeSandbox;
import com.jay.aigcoj.judge.codesandbox.impl.LocalCodeSandbox;
import com.jay.aigcoj.judge.codesandbox.impl.RemoteCodeSandbox;
import com.jay.aigcoj.judge.codesandbox.impl.ThirdPartyCodeSandbox;

/**
 * 代码沙箱工厂（根据字符串参数创建指定的代码沙箱实例）
 */
public class CodeSandboxFactory {

    /**
     * 创建代码沙箱示例
     *
     * @param type 沙箱类型
     * @return
     */
    public static CodeSandbox newInstance(String type) {
        switch (type) {
            case "example":
                return new ExampleCodeSandbox();
            case "remote":
                return new RemoteCodeSandbox();
            case "thirdParty":
                return new ThirdPartyCodeSandbox();
            case "ai":
                return new AICodeSandbox();
            case "local":
                return new LocalCodeSandbox();
            default:
                return new ExampleCodeSandbox();
        }
    }
}
