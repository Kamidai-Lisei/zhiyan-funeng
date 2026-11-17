package com.jay.aigcoj.bot.queryanswer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jay.aigcoj.bot.pojo.DataOrParentComment;
import com.jay.aigcoj.bot.pojo.QRoot;
import com.jay.aigcoj.bot.pojo.ACurrentComment;
import com.jay.aigcoj.bot.pojo.ARoot;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class BotImpl implements Bot {

    private final String userId = "1886334158867619845";
    private final String userName = "AI助手";
    private final String userAvatar = "https://tse4-mm.cn.bing.net/th/id/OIP-C.K_QFIZsqhnGF2Uos2JXzpAHaHa?rs=1&pid=ImgDetMain";
    private final String cookie = "SESSION=NGM0OThiNDAtNWNhOS00YzUwLWIwZmQtZWM2NzdkYzk4NDI1";

    @Override
    public QRoot queryUnAnsweredouestionsTopicId(String quanestionId) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet("http://localhost:8121/api/question_comment/getCommentList?id="+quanestionId);
        get.addHeader("cookie",cookie);
        get.addHeader("Content-Type","application/json");
        CloseableHttpResponse response = httpClient.execute(get);
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String jsonStr = EntityUtils.toString(response.getEntity());
            return JSON.parseObject(jsonStr, QRoot.class);
        }else{
            throw new RuntimeException("requestRoot Err Code is " + response.getStatusLine().getStatusCode());

        }
    }

    @Override
    public boolean answer(QRoot requestRoot, String content) throws IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post= new HttpPost("http://localhost:8121/api/question_comment/wrap/addComment");

        post.addHeader("cookie",cookie);
        post.addHeader("Content-Type","application/json");
        post.addHeader("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36");

        List<DataOrParentComment> data = requestRoot.getData();
        ARoot responseRoot = null;
        for (DataOrParentComment dataAndParentComment : data) {
            if (dataAndParentComment.getReply().isEmpty()){
                String questionId = dataAndParentComment.getQuestionId();
                String parentId = dataAndParentComment.getId();
                String fromId = dataAndParentComment.getUserId();
                String fromName = dataAndParentComment.getUserName();
                String likeListId = "[]";
                ACurrentComment responseCurrentComment = new ACurrentComment(questionId,userId,userName,userAvatar,content,parentId,likeListId,fromId,fromName);
                responseRoot = new ARoot(responseCurrentComment,dataAndParentComment);
            }
        }
        String paramJson = JSONObject.toJSONString(responseRoot);
        StringEntity stringEntity = new StringEntity(paramJson, ContentType.create("text/json","UTF-8"));
        post.setEntity(stringEntity);
        CloseableHttpResponse response = httpClient.execute(post);
        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return true;
        }else{
            throw new RuntimeException("responseRoot Err Code is " + response.getStatusLine().getStatusCode());
        }
    }
}
