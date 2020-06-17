package com.example.demo.spring.framework.context;

import com.example.demo.spring.framework.annotation.MyAutowired;
import com.example.demo.spring.framework.annotation.MyController;
import com.example.demo.spring.framework.annotation.MyService;
import com.example.demo.spring.framework.beans.MyBeanWrapper;
import com.example.demo.spring.framework.beans.config.MyBeanDefinition;
import com.example.demo.spring.framework.beans.support.MyBeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyApplicationContext {


    private MyBeanDefinitionReader reader;

    private Map<String ,MyBeanDefinition> beanDefinitionMap = new HashMap<String, MyBeanDefinition>();

    private Map<String,Object> factoryBeanObjectCache = new HashMap<String, Object>();
    private Map<String,MyBeanWrapper> factoryBeanInstanceCache = new HashMap<String, MyBeanWrapper>();

    public MyApplicationContext(String... contextConfigLocation) {

        //1.加载配置文件
        reader = new MyBeanDefinitionReader(contextConfigLocation);
        //2、解析配置文件，封装成BeanDefinition
        List<MyBeanDefinition>  beanDefinitions = reader.loadBeanDefinitions();

        //3、把BeanDefintion缓存起来
        try {
            doRegistBeanDefinition(beanDefinitions);
            //4.初始化bean到ioc容器
            doAutowirted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doAutowirted() {
        for (Map.Entry<String, MyBeanDefinition> stringMyBeanDefinitionEntry : beanDefinitionMap.entrySet()) {
           String className =  stringMyBeanDefinitionEntry.getKey();
           getBean(className);
        }
    }

    public Object getBean(String beanName) {
        //1、先拿到BeanDefinition配置信息
        MyBeanDefinition myBeanDefinition = this.beanDefinitionMap.get(beanName);
        //2、反射实例化newInstance();
        Object instance = instantiateBean(beanName,myBeanDefinition);
        //3、封装成一个叫做BeanWrapper
        MyBeanWrapper myBeanWrapper = new MyBeanWrapper(instance);
        //4、保存到IoC容器
        factoryBeanInstanceCache.put(beanName,myBeanWrapper);
        //5、依赖注入
        populateBean(beanName,myBeanDefinition,myBeanWrapper);
        return instance;
    }

    private void populateBean(String beanName, MyBeanDefinition myBeanDefinition, MyBeanWrapper myBeanWrapper) {
        Class<?> clazz = myBeanWrapper.getWrapperClass();
        Object instance = myBeanWrapper.getWrapperInstance();

        if(!(clazz.isAnnotationPresent(MyController.class)||clazz.isAnnotationPresent(MyService.class))){
            return;
        }

        for (Field declaredField : clazz.getDeclaredFields()) {

            if(!declaredField.isAnnotationPresent(MyAutowired.class)){
                continue;
            }
            MyAutowired annotation = declaredField.getDeclaredAnnotation(MyAutowired.class);
            String autowiredBeanName = annotation.value().trim();
            if("".equals(autowiredBeanName)){
                autowiredBeanName = declaredField.getType().getName();
            }
            declaredField.setAccessible(true);
            try {
                if(null == this.factoryBeanInstanceCache.get(autowiredBeanName)){continue;}
                declaredField.set(instance ,this.factoryBeanInstanceCache.get(autowiredBeanName) );
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

    private Object instantiateBean(String beanName, MyBeanDefinition myBeanDefinition)  {
        String beanClassName = myBeanDefinition.getBeanClassName();
        Object instance = null;
        try{
            Class<?> clazz = Class.forName(beanClassName);
            instance = clazz.newInstance();
            this.factoryBeanObjectCache.put(beanName,instance);
        }catch (Exception e){
            e.printStackTrace();
        }
        return instance;
    }

    public Object getBean(Class clazz) {
        return getBean(clazz.getName());
    }

    private void doRegistBeanDefinition(List<MyBeanDefinition> beanDefinitions) throws Exception{
        for (MyBeanDefinition beanDefinition : beanDefinitions) {
            if(beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                throw new Exception("the bean:" +beanDefinition.getFactoryBeanName()+" is exist");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(),beanDefinition);
        }
    }
}
