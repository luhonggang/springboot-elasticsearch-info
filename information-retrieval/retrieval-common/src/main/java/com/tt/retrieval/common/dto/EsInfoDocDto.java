package com.tt.retrieval.common.dto;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 资讯类实体
 * @author LuHongGang
 * @version 1.0
 * @date 2020/4/28 18:09
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class EsInfoDocDto implements Serializable {
    private String infoId;
    private String infoTitle;
    private String content;
    private String userCacheName;
    private String comment;
    private String admire;
//    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private String createTime;
}
