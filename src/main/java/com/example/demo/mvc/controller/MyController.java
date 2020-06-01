package com.example.demo.mvc.controller;


import com.example.demo.annotation.MyAutowired;
import com.example.demo.annotation.MyRequestMapping;
import com.example.demo.annotation.MyRequestParam;
import com.example.demo.mvc.service.MyService;
import com.example.demo.mvc.service.impl.MyServiceImpl1;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@com.example.demo.annotation.MyController
@MyRequestMapping("my")
public class MyController {

    @MyAutowired
    private MyService myService;


    @MyRequestMapping("/index")
    public void index(HttpServletRequest req, HttpServletResponse resp, @MyRequestParam("name") String name){

        String result =  myService.index(name);

        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
