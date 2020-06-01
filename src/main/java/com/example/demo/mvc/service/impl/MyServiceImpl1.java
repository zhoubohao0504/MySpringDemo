package com.example.demo.mvc.service.impl;

import com.example.demo.mvc.service.MyService;

@com.example.demo.annotation.MyService
public class MyServiceImpl1 implements MyService {
    public String index(String name) {

        return "hello," +name;
    }
}
