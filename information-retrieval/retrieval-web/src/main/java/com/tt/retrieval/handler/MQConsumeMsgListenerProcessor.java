package com.tt.retrieval.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tt.retrieval.common.GlobalIndexEnum;
import com.tt.retrieval.common.StringUtils;
import com.tt.retrieval.common.dto.ResultDto;
import com.tt.retrieval.common.vo.TimeLineBasicInfoVo;
import com.tt.retrieval.service.RestHighLevelClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author LuHongGang
 * @version 1.0
 */
@Slf4j
@Component
public class MQConsumeMsgListenerProcessor implements MessageListenerConcurrently {

    private final String CHART_SET = "UTF-8";

    @Resource
    private RestHighLevelClientService restHighLevelClientService;

    /**
     * 默认 messageExtList 里只有一条消息，可以通过设置consumeMessageBatchMaxSize参数来批量接收消息<br/>
     * 不要抛异常，如果没有return CONSUME_SUCCESS ，consumer会重新消费该消息，直到return CONSUME_SUCCESS
     */
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messageExtList, ConsumeConcurrentlyContext context) {
        if (CollectionUtils.isEmpty(messageExtList)) {
            log.info("接受到的消息为空，不处理，直接返回成功");
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
        MessageExt msg = messageExtList.get(0);
        Message receiveMsg = messageExtList.get(0);
//                        System.out.printf("%s Receive New Messages: %s %n",
//                                Thread.currentThread().getName(), new String(message.get(0).getBody()));
        try {
           /* if (msg.getTopic().equals(RocketMqConsumeConfig.CONSUME_TOPIC)) {
                if (msg.getTags().equals("tag")) {
                    //TODO 判断该消息是否重复消费（RocketMQ不保证消息不重复,据业务判断是否去重）
                    //TODO 获取该消息重试次数
                    int reconsume = msg.getReconsumeTimes();
                    if (reconsume == 3) {//消息已经重试了3次，如果不需要再次消费，则返回成功
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    }
                    //TODO 处理对应的业务逻辑
                }
            }*/

            String body = new String(receiveMsg.getBody(), CHART_SET);
            // 如果没有return success ,consumer 会重新消费该消息,直到return success
            return putStructuredObjectsToEs(body).getSuccess() ? ConsumeConcurrentlyStatus.CONSUME_SUCCESS : ConsumeConcurrentlyStatus.RECONSUME_LATER;
        } catch (Exception e) {
            e.printStackTrace();
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }

    /**
     * 数据存入ES
     *
     * @param body 原始数据
     */
    private ResultDto putStructuredObjectsToEs(String body) {
            TimeLineBasicInfoVo basicInfoVo = JSON.toJavaObject(JSONObject.parseObject(body), TimeLineBasicInfoVo.class);
            // 进行数据解析
            String prefectContent = StringUtils.replaceHtml(basicInfoVo.getContent());
            basicInfoVo.setContent(prefectContent);
            log.info("body :{}", body);
            ResultDto resultDto;
            // 存入ES
//            if (Objects.nonNull(threadLocal.get())) {
                // TODO 针对重复记录ES 作何处理
                 resultDto = restHighLevelClientService.createInfo(GlobalIndexEnum.GLOBAL_INDEX.getValues(), basicInfoVo.getId(), JSON.toJSONString(basicInfoVo));
//            } else {
//                // TODO 创建索引JSON.toJSONString(basicInfoVo) 数据结构具体字段未做任何具体的声明 第一次创建肯定成功 成功后需要进行数据新增
//                Settings settings = Settings.builder()
//                        .put("index.number_of_shards", 5)
//                        .put("index.number_of_replicas", 2).build();
//                resultDto = restHighLevelClientService.createIndex(GlobalIndexEnum.GLOBAL_INDEX.getValues(), settings, JSON.toJSONString(basicInfoVo));
//                threadLocal.set(String.format("%s:%s",Thread.currentThread().getId(),"create"));
//            }
            return resultDto;
    }

}
