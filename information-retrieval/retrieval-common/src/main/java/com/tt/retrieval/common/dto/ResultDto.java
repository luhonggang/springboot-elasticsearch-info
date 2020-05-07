package com.tt.retrieval.common.dto;

import com.tt.retrieval.common.ResultCodeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LuHongGang
 * @version 1.0
 */
@Accessors(chain = true)
@Data
public class ResultDto {
        // 响应是否成功
        private Boolean success;

        // 响应码
        private Integer code;

        // 响应消息
        private String message;

        // 响应数据
        private Map<String, Object> data = new HashMap<>();

        private Object dataList;

        // 私有构造器
        private ResultDto() {

        }

        // 通用成功
        public static ResultDto ok() {
            return new ResultDto()
                    .setSuccess(ResultCodeEnum.SUCCESS.getSuccess())
                    .setCode(ResultCodeEnum.SUCCESS.getCode())
                    .setMessage(ResultCodeEnum.SUCCESS.getMessage());
        }

        // 通用失败
        public static ResultDto error() {
            return new ResultDto()
                    .setSuccess(ResultCodeEnum.UNKNOWN_ERROR.getSuccess())
                    .setCode(ResultCodeEnum.UNKNOWN_ERROR.getCode())
                    .setMessage(ResultCodeEnum.UNKNOWN_ERROR.getMessage());
        }

        // 自定义返回信息
        public static ResultDto setResult(ResultCodeEnum result) {
            return new ResultDto()
                    .setSuccess(result.getSuccess())
                    .setCode(result.getCode())
                    .setMessage(result.getMessage());
        }

        // 自定义返回信息
        public static ResultDto setResult(ResultCodeEnum result, String message) {
            return new ResultDto()
                    .setSuccess(result.getSuccess())
                    .setCode(result.getCode())
                    .setMessage(message);
        }


        /** ------------使用链式编程，返回类本身-----------**/

        public ResultDto dataList(Object o) {
            this.setDataList(o);
            return this;
        }


    // 自定义返回数据
        public ResultDto data(Map<String, Object> map) {
            this.setData(map);
            return this;
        }

        // 通用设置data
        public ResultDto data(String key, Object value) {
            this.data.put(key, value);
            return this;
        }

        // 自定义状态信息
        public ResultDto message(String message) {
            this.setMessage(message);
            return this;
        }

        // 自定义状态码
        public ResultDto code(Integer code) {
            this.setCode(code);
            return this;
        }

        // 自定义返回结果
        public ResultDto success(Boolean success) {
            this.setSuccess(success);
            return this;
        }
}
