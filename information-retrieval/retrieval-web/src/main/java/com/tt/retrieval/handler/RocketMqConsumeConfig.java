package com.tt.retrieval.handler;

/**
 * @author LuHongGang
 * @version 1.0
 */
public class RocketMqConsumeConfig {
    /**
     * 端口
     */
    public static final String CONSUME_SERVER = "192.168.3.15:9876";

    /**
     * topic,消息依赖于topic
     */
    public static final String CONSUME_TOPIC = "search_es";
}
