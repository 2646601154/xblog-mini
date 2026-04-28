package com.xblog.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ArticleStatus {
    DRAFT("draft"),
    PUBLISHED("published"),
    RECYCLED("recycled");

    private final String value;
}