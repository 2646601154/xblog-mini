package com.xblog;

import com.alibaba.fastjson2.JSON;
import com.xblog.entity.User;
import com.xblog.service.impl.UserServiceImpl;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TestUserServiceImpl {

    @Resource
    private UserServiceImpl userService;
    @Test
    public void test() {
        List<User> list = userService.list();
        String json = JSON.toJSONString(list);
        System.out.println(json);
    }
}
