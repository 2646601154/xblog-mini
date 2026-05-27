package com.xblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xblog.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("""
        SELECT id AS sourceId, nickname AS sourceName, avatar AS url
        FROM user
        WHERE avatar IS NOT NULL AND avatar != ''
        ORDER BY id DESC
    """)
    java.util.List<com.xblog.vo.MediaImageVo> getAvatars();

    @Select("""
        SELECT COUNT(*)
        FROM user
        WHERE avatar = #{url}
    """)
    int countByAvatar(String url);
}