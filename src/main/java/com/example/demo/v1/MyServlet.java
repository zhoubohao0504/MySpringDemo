package com.example.demo.v1;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MyServlet  extends HttpServlet {

    private Properties contextConfig = new Properties();

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
        //==============IoC部分==============
        //3、初始化IoC容器，将扫描到的相关的类实例化，保存到IcC容器中
        //AOP，新生成的代理对象

        //==============DI部分==============
        //4、完成依赖注入
        //==============MVC部分==============
        //5、初始化HandlerMapping
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

}
