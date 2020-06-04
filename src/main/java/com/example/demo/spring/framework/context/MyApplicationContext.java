package com.example.demo.spring.framework.context;

import com.example.demo.spring.framework.beans.config.MyBeanDefinition;
import com.example.demo.spring.framework.support.MyBeanDefinitionReader;

public class MyApplicationContext {


    private MyBeanDefinitionReader reader;

    public MyApplicationContext(String... contextConfigLocation) {

        reader = new MyBeanDefinitionReader(contextConfigLocation);
    }
}
