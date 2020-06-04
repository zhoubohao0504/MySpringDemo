package com.example.demo.spring.framework.context;

import com.example.demo.spring.framework.beans.config.MyBeanDefinition;
import com.example.demo.spring.framework.support.MyBeanDefinitionReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyApplicationContext {


    private MyBeanDefinitionReader reader;

    private Map<String ,MyBeanDefinition> beanDefinitionMap = new HashMap<String, MyBeanDefinition>();

    public MyApplicationContext(String... contextConfigLocation) {

        //1.加载配置文件
        reader = new MyBeanDefinitionReader(contextConfigLocation);
        //2、解析配置文件，封装成BeanDefinition
        List<MyBeanDefinition>  beanDefinitions = reader.loadBeanDefinitions();

        //3、把BeanDefintion缓存起来
        doRegistBeanDefinition(beanDefinitions);
    }

    private void doRegistBeanDefinition(List<MyBeanDefinition> beanDefinitions) {
        for (MyBeanDefinition beanDefinition : beanDefinitions) {
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
        }
    }
}
