package com.example.demo.mvc.controller;


import com.example.demo.spring.framework.annotation.MyAutowired;
import com.example.demo.spring.framework.annotation.MyRequestMapping;
import com.example.demo.spring.framework.annotation.MyRequestParam;
import com.example.demo.mvc.service.TestService;
import com.example.demo.spring.framework.annotation.MyController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@MyController
@MyRequestMapping("my")
public class TestController {

    @MyAutowired
    private TestService testService;


    @MyRequestMapping("/index")
    public void index(HttpServletRequest req, HttpServletResponse resp, @MyRequestParam("name") String name){

        String result =  testService.index(name);

        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
