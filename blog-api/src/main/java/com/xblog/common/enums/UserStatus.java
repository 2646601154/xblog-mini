package com.xblog.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    NORMAL("normal"),
    DISABLED("disabled");

    private final String value;
}