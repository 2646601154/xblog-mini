package com.xblog.common.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xblog.entity.PageResult;

public class PageUtil {
    //将MP查出来的page对象转换成自定义的 PageResult 对象
    public static <T> PageResult<T> build(Page<T> page){
        PageResult<T> result = new PageResult<>();
        result.setRecords(page.getRecords());
        result.setTotal(page.getTotal());
        result.setPage((int) page.getCurrent());
        result.setSize((int) page.getSize());
        return result;
    }
}
