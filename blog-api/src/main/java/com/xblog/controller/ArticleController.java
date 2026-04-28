package com.xblog.controller;

import com.xblog.dto.QueryArticleDto;
import com.xblog.entity.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/articles")
public class ArticleController {

    @GetMapping
    public Result queryArticleList(QueryArticleDto queryArticleDto) {

        return Result.success();
    }
}
