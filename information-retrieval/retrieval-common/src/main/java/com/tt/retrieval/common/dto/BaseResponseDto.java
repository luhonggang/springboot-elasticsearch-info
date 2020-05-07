package com.tt.retrieval.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author LuHongGang
 * @version 1.0
 * @date 2020/5/4 8:54
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseResponseDto<T> {
    Integer code;
    String message;
    T data;
}
