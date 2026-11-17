package com.jay.aigcoj.bot.ds;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.jay.aigcoj.bot.queryanswer.BotImpl;
import com.jay.aigcoj.bot.pojo.DataOrParentComment;
import com.jay.aigcoj.bot.pojo.QRoot;
import com.jay.aigcoj.service.QuestionService;
import com.jay.aigcoj.service.SwitchService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@Service
@EnableScheduling
public class DSAnswer {
    @Resource
    private BotImpl botApi;
    @Resource
    private DSApi deepSeekApi;
    @Resource
    private QuestionService questionService;
    @Resource
    private SwitchService switchService;

    @Scheduled(cron = "0/2 * * * * ?")
    public void answer() throws IOException, UnirestException {

        long status = switchService.getStatus(2);
        if (status == 0) return;

        List<Long> questionIds = questionService.getQuestionId();
        for (Long id: questionIds) {
            QRoot requestRoot = botApi.queryUnAnsweredouestionsTopicId(id.toString());
            List<DataOrParentComment> data = requestRoot.getData();
            String content = null;
            for (DataOrParentComment dataAndParentComment : data) {
                if (dataAndParentComment.getReply().isEmpty()){
                    content = dataAndParentComment.getContent().trim();
                    String questionId = dataAndParentComment.getQuestionId();
                    String questionName = questionService.getQuestionName(Long.valueOf(questionId)).trim();
                    String questionContent = questionService.getQuestionContent(Long.valueOf(questionId)).trim();
                    String myquestion = "你是一个OJ判题系统的智能回复助手，你正处于" + questionName + "这道题的评论区，这道题的具体内容是" + questionContent +
                            "有人在评论区评论：" + content +
                            "请你帮他解答问题，当他的问题与这个题目无关的时候，你不要输出与题目相关的内容" +
                            "输出的最大长度是1000字节，你的回答尽可能简洁且输出内容要在同一行内，不要使用加粗和无序有序列表以及代码块";
                    //根据查到的问题，AI进行回答
                    String answer = deepSeekApi.sendDeepseekChat(myquestion);
                    botApi.answer(requestRoot, answer);
                }
            }
        }
    }
}
