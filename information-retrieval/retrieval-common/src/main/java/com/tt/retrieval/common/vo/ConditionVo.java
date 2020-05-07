package com.tt.retrieval.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author LuHongGang
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class ConditionVo {
    private Integer pageIndex = 1;
    private Integer pageSize = 10;
    private InfoVo infoVo;

    public Integer getPageIndex() {
        return this.pageIndex > 0 ? (pageIndex-1)*pageSize : pageIndex;
    }

    public Integer getPageSize() {
//        return this.pageSize < 0 ? 10:pageSize;
        return this.pageSize < 10 ? 10:pageSize;
    }
}
