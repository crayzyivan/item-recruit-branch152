package com.item.framework.http;

import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

/**
 * <p>
 * 前端分页对象
 * </p>
 *
 * @author liuyabin on 2025/7/9
 * @since 1.0.0
 */
@Data
public class Pager<T> implements Serializable {
    /**
     * 当前页码
     */
    private long pageIndex;
    /**
     * 每页数量
     */
    private long pageSize;
    /**
     * 总页数
     */
    private long totalPage;
    /**
     * 总数据数量
     */
    private long totalCount;
    /**
     * 当前页数据
     */
    private List<T> currentPageRecords;
    public long getTotalPage(){
        if(pageSize == 0) return 0;
        totalPage = totalCount%pageSize ==0? (totalCount/pageSize) : (totalCount/pageSize) +1;

        return totalPage;
    }

    public static <R,T> Pager<T> build(com.baomidou.mybatisplus.core.metadata.IPage<R> page,
                                     Function<List<R>, List<T>> func) {
        Pager<T> pageResult = new Pager<>();
        pageResult.setCurrentPageRecords(func.apply(page.getRecords()));
        pageResult.setPageIndex(page.getCurrent());
        pageResult.setPageSize(page.getSize());
        pageResult.setTotalCount(page.getTotal());

        return pageResult;
    }

    public static <R,T> Pager<T> build(Pager<R> page,
                                       Function<List<R>, List<T>> func) {
        Pager<T> pageResult = new Pager<>();
        pageResult.setCurrentPageRecords(func.apply(page.getCurrentPageRecords()));
        pageResult.setPageIndex(page.getPageIndex());
        pageResult.setPageSize(page.getPageSize());
        pageResult.setTotalCount(page.getTotalCount());

        return pageResult;
    }

    /**
     * 构建空数据
     * @param <T>
     * @return
     */
    public static <T> Pager<T> buildEmpty(){
        Pager<T> pageResult = new Pager<>();
        pageResult.setCurrentPageRecords(Lists.newArrayList());
        pageResult.setPageIndex(0);
        pageResult.setPageSize(0);
        pageResult.setTotalCount(0);

        return pageResult;
    }
}
