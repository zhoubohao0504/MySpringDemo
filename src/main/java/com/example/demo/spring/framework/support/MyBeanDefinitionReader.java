package com.example.demo.spring.framework.support;

import com.example.demo.spring.framework.beans.config.MyBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MyBeanDefinitionReader {

    //保存扫描的结果
    private List<String> regitryBeanClasses = new ArrayList<String>();
    private Properties contextConfig = new Properties();

    public MyBeanDefinitionReader(String... contextConfigLocation) {
        //读取配置文件内容
        doLoadConfig(contextConfigLocation[0]);
        doScanner(contextConfig.getProperty("scanPackage"));
    }

    private void doScanner(String scanPackage) {

        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        //将获取到的路径转换为文件形式
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            //如果当前扫描到的是一个文件夹，则将当前文件夹拼接到后面 重新进行扫描
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                //判断下只获取.class文件
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                //全类名 = 包名.类名
                String className = (scanPackage + "." + file.getName().replace(".class", ""));
                //Class.forName(className);
                //将扫描到文件保存起来
                regitryBeanClasses.add(className);
            }
        }
    }

    private void doLoadConfig(String contextConfigLocation) {

        //读取wen.xml里面配置的init-param 从而拿到指定的文件路径
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation.replaceAll("classPath:", ""));
        try {
            //将获取到的文件流通过键值对的形式放入到Properties里面
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<MyBeanDefinition> loadBeanDefinitions() {
        List<MyBeanDefinition> list = new ArrayList<MyBeanDefinition>();

        for (String regitryBeanClass : regitryBeanClasses) {
            try {
                //1.正常的
                Class<?> clazz = Class.forName(regitryBeanClass);
                list.add(new MyBeanDefinition(toLowerFirstCase(clazz.getSimpleName()),clazz.getName()));
                //2.接口注入
                for (Class<?> anInterface : clazz.getInterfaces()) {
                    list.add(new MyBeanDefinition(toLowerFirstCase(anInterface.getSimpleName()),clazz.getName()));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return list;
    }


    //自己写，自己用
    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
//        if(chars[0] > )
        chars[0] += 32;
        return String.valueOf(chars);
    }

}
