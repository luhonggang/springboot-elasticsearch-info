package com.tt.retrieval.common.vo;

import lombok.*;

/**
 * @author LuHongGang
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class InfoVo {
    private String infoName;
    private String content;
    private String userCacheName;
}
