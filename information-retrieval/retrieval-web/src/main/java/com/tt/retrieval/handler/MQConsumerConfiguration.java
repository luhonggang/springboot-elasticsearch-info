package com.tt.retrieval.handler;

import com.tt.retrieval.common.BaseException;
import com.tt.retrieval.common.ResultCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * @author LuHongGang
 * @version 1.0
 */
@Slf4j
@Configuration
public class MQConsumerConfiguration {
    @Value("${rocketMq.consumer.namesAddr}")
    private String namesAddr;
    @Value("${rocketMq.consumer.groupName}")
    private String groupName;
    @Value("${rocketMq.consumer.topics}")
    private String topics;
    @Value("${rocketMq.consumer.consumeThreadMin}")
    private int consumeThreadMin;
    @Value("${rocketMq.consumer.consumeThreadMax}")
    private int consumeThreadMax;
    @Value("${rocketMq.consumer.consumeMessageBatchMaxSize}")
    private int consumeMessageBatchMaxSize;
    @Resource
    private MQConsumeMsgListenerProcessor mqMessageListenerProcessor;

    @Bean
    @Primary
    public DefaultMQPushConsumer getRocketMQConsumer() {
        Assert.notNull(groupName,"groupName is null");
        Assert.notNull(namesAddr,"namesAddr is null");
        Assert.notNull(topics,"topics is null");
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupName);
        consumer.setNamesrvAddr(namesAddr);
        consumer.setConsumeThreadMin(consumeThreadMin);
        consumer.setConsumeThreadMax(consumeThreadMax);
        consumer.registerMessageListener(mqMessageListenerProcessor);
        /*
         * 设置Consumer第一次启动是从队列头部开始消费还是队列尾部开始消费
         * 如果非第一次启动，那么按照上次消费的位置继续消费
         */
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        /*
         * 设置消费模型，集群还是广播，默认为集群
         */
        //consumer.setMessageModel(MessageModel.CLUSTERING);
        /*
         * 设置一次消费消息的条数，默认为1条
         */
        consumer.setConsumeMessageBatchMaxSize(consumeMessageBatchMaxSize);
        try {
            /*
             * 设置该消费者订阅的主题和tag，如果是订阅该主题下的所有tag，则tag使用*；如果需要指定订阅该主题下的某些tag，则使用||分割，例如tag1||tag2||tag3
             */
//            String[] topicTagsArr = topics.split(";");
//            for (String topicTags : topicTagsArr) {
//                String[] topicTag = topicTags.split("~");
//                consumer.subscribe(topicTag[0],topicTag[1]);
//            }
            // 订阅主题的哪些标签
            consumer.subscribe(topics, "*");
            consumer.start();
            log.info("consumer is start !!! groupName:{},topics:{},namesAddr:{}",groupName,topics,namesAddr);
        }catch (MQClientException e){
            log.error("consumer is start !!! groupName:{},topics:{},namesAddr:{}",groupName,topics,namesAddr,e);
            throw new BaseException(ResultCodeEnum.MQ_ERROR);
        }
        return consumer;
    }
}
