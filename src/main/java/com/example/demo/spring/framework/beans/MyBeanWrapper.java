package com.example.demo.spring.framework.beans;

public class MyBeanWrapper {

    private Object wrapperInstance;
    private Class<?> wrapperClass;

    public MyBeanWrapper(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
        this.wrapperClass = wrapperInstance.getClass();
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public Class<?> getWrapperClass() {
        return wrapperClass;
    }
}
