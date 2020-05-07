package com.tt.retrieval.common.vo;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

/**
 * @author LuHongGang
 * @version 1.0
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class EsInfoDocVo implements Serializable {
    private String infoId;
    private String infoTitle;

    @Field(index = true,type = FieldType.Text, analyzer = "ik_max_word",searchAnalyzer = "ik_max_word")
    private String content;
    @Field(index = true,type = FieldType.Text, analyzer = "ik_smart",searchAnalyzer = "ik_smart")
    private String userCacheName;
    private String comment;
    private String admire;
    //    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private String createTime;
}
