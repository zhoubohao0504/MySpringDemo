package com.example.demo.mvc.service.impl;

import com.example.demo.mvc.service.TestService;
import com.example.demo.spring.framework.annotation.MyService;

@MyService
public class TestServiceImpl1 implements TestService {
    public String index(String name) {

        return "hello," +name;
    }
}
