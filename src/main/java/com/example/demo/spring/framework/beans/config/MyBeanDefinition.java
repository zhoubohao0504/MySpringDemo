package com.example.demo.spring.framework.beans.config;

public class MyBeanDefinition {

    private String factoryBeanName;
    private String beanClassName;

    public MyBeanDefinition() {
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }
}
