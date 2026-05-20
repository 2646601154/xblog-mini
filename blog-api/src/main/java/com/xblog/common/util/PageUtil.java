package com.xblog.common.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xblog.entity.PageResult;

import java.util.List;
import java.util.function.Function;

/**
 * MyBatis-Plus Page → 自定义 PageResult 转换工具
 */
public class PageUtil {

    /**
     * 分页参数 null 安全兜底
     */
    public static int pageNum(Integer page) {
        return page != null ? page : 1;
    }

    public static int pageSize(Integer size) {
        return size != null ? size : 10;
    }

    /**
     * 通用分页参数提取（适用于任意 DTO 类型）
     */
    public static <T> int pageNum(T dto, Function<T, Integer> getter) {
        Integer page = getter.apply(dto);
        return page != null ? page : 1;
    }

    public static <T> int pageSize(T dto, Function<T, Integer> getter) {
        Integer size = getter.apply(dto);
        return size != null ? size : 10;
    }

    /**
     * 将 MP 分页结果直接转换为 PageResult（records 透传）
     */
    public static <T> PageResult<T> build(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPage((int) page.getCurrent());
        result.setSize((int) page.getSize());
        return result;
    }

    /**
     * 将 MP 分页结果转换为 PageResult，并替换 records 内容（VO 转换场景）
     */
    public static <T> PageResult<T> build(Page<?> page, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(page.getTotal());
        result.setPage((int) page.getCurrent());
        result.setSize((int) page.getSize());
        return result;
    }
}
