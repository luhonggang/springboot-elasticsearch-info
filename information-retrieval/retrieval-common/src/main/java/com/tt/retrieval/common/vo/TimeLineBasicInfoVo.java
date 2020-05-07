package com.tt.retrieval.common.vo;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ToString
@Builder
public class TimeLineBasicInfoVo implements Serializable {

    private static final long serialVersionUID = 3491764272435494529L;

    @Id
    private String id;

    private String source;  // 来源名称

    private Integer sourceType;  // 来源类型： 1、微博 2、微信 3、雪球 4、同花顺 5、淘股吧 6、东方财富  7、财联社

    private Integer targetType;  // 目标类型： 1、全球资讯 2、美股资讯 3、港股资讯 4、黄金资讯
                                 // 5、亚太市场资讯 6、欧洲资讯 7、板块情报 8、要闻  31、港股个股资讯  21、美股个股资讯
    private Integer market;  // 目标市场类型： 1、沪市 2、深市 3、港股 4、美股

    private String type;  // 类型

    private String sourceId;  // 源id

    private String sourceUid;  // 源作者id

    private String author;  // 源作者名称

    private Long authorId;  // 作者Id,本地数据库维护的作者id，部分大V的文章会有

    private String sourceProfile;  // 源作者头像

    private String sourceUrl;  // 来源url

    private String title;  // 标题

    private String description; // 摘要

    private String picList; // 封面图片列表，英文逗号,分割

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sourceDate; // 源数据发布时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate; // 抓取入库时间

    private long readCount;  // 阅读数

    private long replyCount;  // 评论数

    private long likeCount;   // 点赞数

    private long retweetCount;  // 转发数

    private long favCount;  // 收藏数

//    private TimeLineBasicInfoVo retweeted; // 转发内容

    private boolean commentFlag;  // 是否抓取过评论

    private boolean labelFlag;  // 是否打过标签

    private String content;  // 正文


    @Override
    public String toString() {
        return "{" +
                "id:'" + id + '\'' +
                ", source:'" + source + '\'' +
                ", sourceType:" + sourceType +
                ", targetType:" + targetType +
                ", market:" + market +
                ", type:'" + type + '\'' +
                ", sourceId:'" + sourceId + '\'' +
                ", sourceUid:'" + sourceUid + '\'' +
                ", author:'" + author + '\'' +
                ", authorId:" + authorId +
                ", sourceProfile:'" + sourceProfile + '\'' +
                ", sourceUrl:'" + sourceUrl + '\'' +
                ", title:'" + title + '\'' +
                ", description:'" + description + '\'' +
                ", picList:'" + picList + '\'' +
                ", sourceDate:" + sourceDate +
                ", createDate:" + createDate +
                ", readCount:" + readCount +
                ", replyCount:" + replyCount +
                ", likeCount:" + likeCount +
                ", retweetCount:" + retweetCount +
                ", favCount:" + favCount +
                ", commentFlag:" + commentFlag +
                ", labelFlag:" + labelFlag +
                ", content:'" + content + '\'' +
                '}';
    }
}
