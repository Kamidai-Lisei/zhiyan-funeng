package com.jay.aigcoj.bot.queryanswer;


import com.jay.aigcoj.bot.pojo.QRoot;

import java.io.IOException;

public interface Bot {
    QRoot queryUnAnsweredouestionsTopicId(String quertionId) throws IOException;

    boolean answer(QRoot requestRoot, String content) throws IOException;
}
