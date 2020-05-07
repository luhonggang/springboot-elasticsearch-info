package com.tt.retrieval.common;

import lombok.Getter;

/**
 * @author LuHongGang
 * @version 1.0
 */
@Getter
public enum ResultCodeEnum {
    SUCCESS(true,200,"成功"),
    UNKNOWN_ERROR(false,201,"未知错误"),
    PARAM_ERROR(false,202,"参数错误"),
    NULL_POINTER(false,203,"空指针异常"),
    VALID_ERROR(false,204,"参数绑定异常"),
    ES_ERROR(false,205,"ES数据同步异常"),
    ES_INDEX_ERROR(false,206,"ES索引参数异常"),
    ES_SETTING_ERROR(false,207,"ES分片全局参数设置异常"),
    ES_MAPPING_ERROR(false,208,"ES存储数据结构异常"),
    INDEX_NOT_EXIST(false,209,"ES索引不存在"),
    MQ_ERROR(false,400,"MQ客户端消费服务异常"),
    SERVER_ERROR(false,500,"服务异常");

    // 响应是否成功
    private Boolean success;
    // 响应状态码
    private Integer code;
    // 响应信息
    private String message;

    ResultCodeEnum(Boolean success, Integer code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }
}
