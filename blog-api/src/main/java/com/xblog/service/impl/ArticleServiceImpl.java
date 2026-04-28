package com.xblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xblog.entity.Article;
import com.xblog.mapper.ArticleMapper;
import com.xblog.service.ArticleService;
import org.springframework.stereotype.Service;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {
}
