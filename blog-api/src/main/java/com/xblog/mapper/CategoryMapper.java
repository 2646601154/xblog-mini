package com.xblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xblog.entity.Category;
import com.xblog.vo.CategoryAdminVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("""
        SELECT
            c.id,
            c.name,
            c.slug,
            c.description,
            c.sort_order AS sortOrder,
            c.created_at AS createdAt,
            COUNT(a.id) AS articleCount
        FROM category c
        LEFT JOIN article a ON c.id = a.category_id AND a.deleted = 0
        GROUP BY c.id
        ORDER BY c.sort_order ASC
    """)
    List<CategoryAdminVo> getAdminCategoryList();
}
