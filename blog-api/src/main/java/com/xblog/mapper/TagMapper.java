package com.xblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xblog.entity.Tag;
import com.xblog.vo.TagAdminVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    @Select("""
        SELECT
            t.id,
            t.name,
            t.slug,
            t.created_at AS createdAt,
            COUNT(at.article_id) AS articleCount
        FROM tag t
        LEFT JOIN article_tag at ON t.id = at.tag_id
        GROUP BY t.id
        ORDER BY articleCount DESC, t.id ASC
    """)
    List<TagAdminVo> getAdminTagList();
}