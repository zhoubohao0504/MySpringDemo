package com.example.demo.v1;


import com.example.demo.annotation.MyController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class MyServlet  extends HttpServlet {

    private Properties contextConfig = new Properties();
    private List<String> classNames = new ArrayList<String>();

    //IoC容器，key默认是类名首字母小写，value就是对应的实例对象
    private Map<String,Object> ioc = new HashMap<String,Object>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1、加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //2、扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
        //==============IoC部分==============
        //3、初始化IoC容器，将扫描到的相关的类实例化，保存到IcC容器中

        doInstance();
        //AOP，新生成的代理对象

        //==============DI部分==============
        //4、完成依赖注入
        //==============MVC部分==============
        //5、初始化HandlerMapping
    }

    private void doInstance() {

        //如果扫描到的文件为空的话直接返回
        if(classNames.isEmpty()){return;}
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                if(clazz.isAnnotationPresent(MyController.class)){
                    String beanName = toLowerCaseFirst(clazz.getSimpleName());
                    Object instance = clazz.newInstance();
                    ioc.put(beanName,instance);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private String toLowerCaseFirst(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0]+=32;
        return String.valueOf(chars);
    }

    private void doScanner(String scanPackage) {
        //获取设置的需要进行扫描的包的路径
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.","/"));
        //将获取到的路径转换为文件形式
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            //如果当前扫描到的是一个文件夹，则将当前文件夹拼接到后面 重新进行扫描
            if(file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            }else {
                //判断下只获取.class文件
                if(!file.getName().endsWith(".class")){continue;}
                //全类名 = 包名.类名
                String className = (scanPackage + "." + file.getName().replace(".class", ""));
                //Class.forName(className);
                classNames.add(className);
            }
        }

    }


    private void doLoadConfig(String contextConfigLocation) {

        //读取wen.xml里面配置的init-param 从而拿到指定的文件路径
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            //将获取到的文件流通过键值对的形式放入到Properties里面
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void main(String[] args) {
        char[] chars = "simpleName".toCharArray();
        chars[0]+=32;
        System.out.printf( String.valueOf(chars));
    }
}
