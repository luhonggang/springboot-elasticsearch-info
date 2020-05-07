package com.tt.retrieval.common.dto;

import lombok.*;

import java.util.List;

/**
 * @author LuHongGang
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class PageBeanDto<T> {
    /**
     * 存放需要显示的实体类数据
     */
    private T lists;
    /**
     * 当前页码数（默认给1）,需要传参
     */
    private Integer pageNo = 1;
    /**
     * 每页显示的行数,需要传参
     */
    private Integer pageSize;
    // this.totalPage = rows % pageSize == 0 ? rows / pageSize : (rows / pageSize + 1);
    /**
     * 总页数,是根据总行数和每页显示的行数计算出来的结果
     */
    private Integer totalPage;
    /**
     * 总行数是查询出来的数据表总记录数
     */
    private Integer rows;

    // 对私有属性的封装
    // 不需要对外提供totalPage总页数的set设值方法,因为totalPage是根据总行数和每页显示的行数求出来的
    public T getLists() {
        return lists;
    }

    public void setLists(T lists) {
        this.lists = lists;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public Integer getRows() {
        return rows;
    }

    /**
     * 设置总行数据并求出总页数
     *
     * @param rows 此参数是总行数
     */
    public void setRows(Integer rows) {
        this.rows = rows;
        //页数根据传入的总行数以及每页显示的行数，求出总页数
        this.totalPage = rows % pageSize == 0 ? rows / pageSize : (rows / pageSize + 1);
    }

    /**
     * 设置页码
     * @param pageNo 当前页数
     */
    public void setPageNo(Integer pageNo) {
        //如果传入的页码为空或者小于0  就默认给1
        if (null == pageNo || pageNo < 0) {
            this.pageNo = 1;
            //如果当前页码数大于总页码数，就让当前页码数等于最大页码数
        } else if (pageNo > this.totalPage && this.totalPage > 0) {
            this.pageNo = this.totalPage;
            //都符合条件就让当前页码数等于传入的页码数
        } else {
            this.pageNo = pageNo;
        }
    }

}
