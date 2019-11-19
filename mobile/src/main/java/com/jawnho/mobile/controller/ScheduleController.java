package com.jawnho.mobile.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Resource;

@Controller
public class ScheduleController {

    private Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    @Resource
    private MongoTemplate mongoTemplate;

    private final RestTemplate restTemplate;

    public ScheduleController() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Scheduled(cron = "0/20 * * * * ?")
    public void schedule() {
        SimpleDateFormat sf = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
        String dateStr = sf.format(new Date());

        String ma2001 = getNewPrice("MA2001");
        if (Strings.isNullOrEmpty(ma2001)) {
            return;
        }

        String pp2001 = getNewPrice("PP2001");
        if (Strings.isNullOrEmpty(pp2001)) {
            return;
        }

        double ma = Double.parseDouble(ma2001);
        double pp = Double.parseDouble(pp2001);

        double diff = pp - ma * 3;
        String result = new BigDecimal(diff).setScale(2).toString();

        String msg =  dateStr + "\tMTO利润:\t" + result;
        logger.info(msg);
        setDingTalk(msg);
    }

    public String getNewPrice(String num) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(num));
        String url = "https://hq.sinajs.cn/?list=nf_" + num;

        ResponseEntity<String> entity = restTemplate.getForEntity(
                url,
                String.class
        );
        String body = entity.getBody();
        if (Strings.isNullOrEmpty(body)) {
            logger.error("body is null");
            return null;
        }

        String[] arr = body
                .substring("var hq_str_nf_PP2001=".length())
                .replace("\"", "")
                .split(",");

        if (arr == null || arr.length < 8) {
            logger.error("arr is null or arr length < 8");
            return null;
        }

        return arr[8];
    }


    public void setDingTalk(String msg) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(msg));
        DingTalkClient client = new DefaultDingTalkClient(
                "https://oapi.dingtalk.com/robot/send?access_token=de6368e670c1b0c8b3a418068057222f747f63a4530729d7de0398c38860ece8"
        );
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("text");
        OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
        text.setContent(msg);
        request.setText(text);
        try {
            client.execute(request);
        } catch (ApiException e) {
            logger.error(msg);
        }
    }

}
