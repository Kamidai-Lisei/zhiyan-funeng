package com.jay.aigcoj.judge.codesandbox.impl;

import com.jay.aigcoj.judge.codesandbox.CodeSandbox;
import com.jay.aigcoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.jay.aigcoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.jay.aigcoj.judge.codesandbox.model.JudgeInfo;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 本地代码沙箱：直接调用本机 JDK / Python 执行代码。
 * 适用于本地演示环境，已做语言白名单、超时与输出长度限制。
 */
@Component
public class LocalCodeSandbox implements CodeSandbox {

    private static final long RUN_TIMEOUT_MS = 5000L;
    private static final long COMPILE_TIMEOUT_MS = 20000L;
    private static final int MAX_OUTPUT = 1024 * 1024;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest request) {
        String language = request.getLanguage() == null ? "" : request.getLanguage().toLowerCase();
        String code = request.getCode();
        List<String> inputList = request.getInputList() == null ? new ArrayList<>() : request.getInputList();

        Path dir = null;
        try {
            dir = Files.createTempDirectory("aigcoj-");
            List<String> command = prepare(language, code, dir);
            if (command == null) {
                return error("Compile Error", "不支持的语言：" + language);
            }
            if (isCompileLanguage(language)) {
                ProcessResult compile = run(command, null, dir, COMPILE_TIMEOUT_MS);
                if (compile.exitCode != 0) {
                    String msg = "Compile Error\n" + trim(compile.output, 800);
                    return error("Compile Error", msg);
                }
            }

            List<String> outputList = new ArrayList<>();
            long maxTime = 0L;
            String runError = null;
            for (String input : inputList) {
                ProcessResult result = run(runCommand(language, dir), input, dir, RUN_TIMEOUT_MS);
                maxTime = Math.max(maxTime, result.timeMs);
                if (result.timeout) {
                    runError = "Time Limit Exceeded";
                    outputList.add("");
                    break;
                }
                if (result.exitCode != 0) {
                    runError = "Runtime Error";
                    outputList.add("");
                    break;
                }
                outputList.add(normalize(result.output));
            }
            // 出错的用例之后的用例补空串，保证 outputList 与 inputList 等长，便于按用例计分
            while (outputList.size() < inputList.size()) {
                outputList.add("");
            }
            JudgeInfo judgeInfo = new JudgeInfo(runError == null ? "Accepted" : runError, 0L, maxTime);
            return new ExecuteCodeResponse(outputList, runError == null ? "ok" : runError, 1, judgeInfo);
        } catch (Exception e) {
            return error("Runtime Error", e.getMessage() == null ? "执行失败" : e.getMessage());
        } finally {
            deleteQuietly(dir);
        }
    }

    private boolean isCompileLanguage(String language) {
        return "java".equals(language) || "cpp".equals(language) || "c".equals(language);
    }

    private List<String> prepare(String language, String code, Path dir) throws Exception {
        switch (language) {
            case "java":
                Files.write(dir.resolve("Main.java"), code.getBytes(StandardCharsets.UTF_8));
                // -J-Duser.language=en 让 javac 诊断输出为英文 ASCII，避免中文 Windows 下 GBK 诊断读成 UTF-8 出现乱码
                return Arrays.asList(javac(), "-J-Duser.language=en", "-J-Duser.country=US",
                        "-encoding", "UTF-8", "-d", dir.toString(), "Main.java");
            case "python":
            case "python3":
                Files.write(dir.resolve("main.py"), code.getBytes(StandardCharsets.UTF_8));
                return Arrays.asList("python", "main.py");
            case "cpp":
                Files.write(dir.resolve("main.cpp"), code.getBytes(StandardCharsets.UTF_8));
                return Arrays.asList(cppCompiler(), "-O2", "-std=" + cppStd(), "-o", "main", "main.cpp");
            default:
                return null;
        }
    }

    private List<String> runCommand(String language, Path dir) {
        switch (language) {
            case "java":
                return Arrays.asList(javaBin(), "-Xmx256m", "-Dfile.encoding=UTF-8", "-cp", dir.toString(), "Main");
            case "python":
            case "python3":
                return Arrays.asList("python", "-X", "utf8", "main.py");
            case "cpp":
                String exe = System.getProperty("os.name", "").toLowerCase().contains("win") ? "main.exe" : "main";
                return Arrays.asList(dir.resolve(exe).toString());
            default:
                return new ArrayList<>();
        }
    }

    private String javac() {
        String home = System.getProperty("java.home");
        String exe = home + File.separator + "bin" + File.separator + "javac";
        File file = new File(exe + ".exe");
        if (file.exists()) {
            return file.getAbsolutePath();
        }
        // JRE 运行或路径异常时退回 PATH 中的 javac
        return new File(exe).exists() ? exe : "javac";
    }

    /**
     * g++ 可能不在 PATH 中（如 Dev-Cpp / MinGW / TDM-GCC / msys64），
     * 依次探测常见安装位置，最后才退回 PATH 查找。
     */
    private String cppCompiler() {
        String[] candidates = {
                "D:" + File.separator + "Dev-Cpp" + File.separator + "MinGW64" + File.separator + "bin" + File.separator + "g++.exe",
                "C:" + File.separator + "MinGW" + File.separator + "bin" + File.separator + "g++.exe",
                "C:" + File.separator + "TDM-GCC-64" + File.separator + "bin" + File.separator + "g++.exe",
                "C:" + File.separator + "msys64" + File.separator + "mingw64" + File.separator + "bin" + File.separator + "g++.exe",
                "C:" + File.separator + "Program Files" + File.separator + "CodeBlocks" + File.separator + "MinGW" + File.separator + "bin" + File.separator + "g++.exe",
        };
        for (String path : candidates) {
            if (new File(path).exists()) {
                return path;
            }
        }
        return "g++";
    }

    /** 老 MinGW（< 5）不认识 -std=c++17，探测一次并缓存可用的标准版本 */
    private String cppStd() {
        if (cachedCppStd != null) {
            return cachedCppStd;
        }
        synchronized (LocalCodeSandbox.class) {
            if (cachedCppStd != null) {
                return cachedStd();
            }
            String std = "c++17";
            try {
                Path probe = Files.createTempDirectory("aigcoj-probe-");
                Files.write(probe.resolve("t.cpp"), "int main(){return 0;}".getBytes(StandardCharsets.UTF_8));
                ProcessResult r = run(Arrays.asList(cppCompiler(), "-O2", "-std=c++17", "-o", "t", "t.cpp"),
                        null, probe, 10000L);
                if (r.exitCode != 0) {
                    std = "c++14";
                }
                deleteQuietly(probe);
            } catch (Exception ignored) {
            }
            cachedCppStd = std;
            return std;
        }
    }

    private String cachedStd() {
        return cachedCppStd;
    }

    private volatile String cachedCppStd;

    private String javaBin() {
        String home = System.getProperty("java.home");
        String exe = home + File.separator + "bin" + File.separator + "java";
        File file = new File(exe + ".exe");
        return file.exists() ? file.getAbsolutePath() : exe;
    }

    private ProcessResult run(List<String> command, String input, Path dir, long timeoutMs) throws Exception {
        long start = System.currentTimeMillis();
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(dir.toFile());
        pb.redirectErrorStream(true);
        pb.environment().put("PYTHONIOENCODING", "UTF-8");
        Process process = pb.start();

        if (input != null) {
            try (OutputStream os = process.getOutputStream()) {
                os.write(input.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Thread reader = new Thread(() -> {
            try (InputStream is = process.getInputStream()) {
                byte[] buf = new byte[4096];
                int len;
                while ((len = is.read(buf)) != -1) {
                    if (buffer.size() < MAX_OUTPUT) {
                        buffer.write(buf, 0, len);
                    }
                }
            } catch (Exception ignored) {
            }
        });
        reader.start();
        boolean finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            process.waitFor(2, TimeUnit.SECONDS);
        }
        reader.join(1000);
        long cost = System.currentTimeMillis() - start;
        // 这里修改：兼容JDK8，去掉 buffer.toString(Charset)
        return new ProcessResult(finished ? process.exitValue() : -1,
                new String(buffer.toByteArray(), StandardCharsets.UTF_8), cost, !finished);
    }

    private String normalize(String output) {
        if (output == null) {
            return "";
        }
        String[] lines = output.replace("\r\n", "\n").split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line.replaceAll("\\s+$", "")).append("\n");
        }
        return sb.toString().trim();
    }

    private String trim(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() > max ? text.substring(0, max) : text;
    }

    private ExecuteCodeResponse error(String message, String detail) {
        JudgeInfo judgeInfo = new JudgeInfo(message, 0L, 0L);
        return new ExecuteCodeResponse(new ArrayList<>(), detail, 3, judgeInfo);
    }

    private void deleteQuietly(Path dir) {
        if (dir == null) {
            return;
        }
        try (java.util.stream.Stream<Path> stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (Exception ignored) {
        }
    }

    private static class ProcessResult {
        final int exitCode;
        final String output;
        final long timeMs;
        final boolean timeout;

        ProcessResult(int exitCode, String output, long timeMs, boolean timeout) {
            this.exitCode = exitCode;
            this.output = output;
            this.timeMs = timeMs;
            this.timeout = timeout;
        }
    }
}
