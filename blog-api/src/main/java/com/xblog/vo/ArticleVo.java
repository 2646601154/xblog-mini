package com.xblog.vo;

import com.xblog.entity.Category;
import com.xblog.entity.Tag;
import com.xblog.entity.User;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ArticleVo {
    private Long id;
    private String title;
    private String summary;
    private String coverImage;
    private Category category;// 分类对象
    private User author;// 作者对象
    private List<Tag> tags;
    private Integer viewCount;
    private LocalDate publishAt;
    private LocalDate createdAt;
}
