package com.tt.retrieval.common;

import lombok.Getter;

/**
 * @author LuHongGang
 * @version 1.0
 */
@Getter
public enum GlobalIndexEnum {
    GLOBAL_INDEX_ALIAS("alias_index_info"),
    GLOBAL_INDEX_INFO("index_info"),
    GLOBAL_TYPE_INFO("t_info"),
    GLOBAL_INFO_ALIAS("global_info_alias"),
    GLOBAL_INDEX("global_index"),
    GLOBAL_TYPE("global_type");
    private String values;

    GlobalIndexEnum(String values) {
        this.values = values;
    }

}
