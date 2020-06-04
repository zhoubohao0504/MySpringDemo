package com.example.demo.spring.framework.beans.config;

public class MyBeanDefinition {

    private String factoryBeanName;
    private String beanClassName;

    public MyBeanDefinition() {
    }

    public MyBeanDefinition(String factoryBeanName, String beanClassName) {
        this.factoryBeanName = factoryBeanName;
        this.beanClassName = beanClassName;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }
}
