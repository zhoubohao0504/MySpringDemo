package com.example.demo.spring.framework.webmvc.servlet;


import com.example.demo.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class MyServlet extends HttpServlet {

    private Properties contextConfig = new Properties();
    private List<String> classNames = new ArrayList<String>();

    //IoC容器，key默认是类名首字母小写，value就是对应的实例对象
    private Map<String,Object> ioc = new HashMap<String,Object>();


    private Map<String,Method> handlerMapping = new HashMap<String, Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception  {

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");
        if(!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404 not found!");
            return;
        }
        Map<String ,String[]> params = req.getParameterMap();
        Method method = this.handlerMapping.get(url);


        Class<?>[] parameterTypes = method.getParameterTypes();
        Object [] parameterValues = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {

            Class<?> parameterType = parameterTypes[i];
            if(parameterType ==HttpServletRequest.class){
                parameterValues[i] = req;
            }else if(parameterType ==HttpServletResponse.class){
                parameterValues[i] =resp;
            }else if (parameterType ==String.class){

                Annotation[] [] pa = method.getParameterAnnotations();
                for (int j = 0; j < pa.length; j++) {
                    for (Annotation a : pa[i]) {

                        if(a instanceof MyRequestParam){
                            String paramName = ((MyRequestParam) a).value();

                            if(!"".equals(paramName.trim())){
                                String value = Arrays.toString(params.get(paramName))
                                        .replaceAll("\\[|\\]","")
                                        .replaceAll("\\s","");
                                parameterValues[i] = value;
                            }
                        }
                    }
                    
                }
            }


        }
        String beanName = toLowerCaseFirst(method.getDeclaringClass().getSimpleName());
        method.invoke(ioc.get(beanName),parameterValues);

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
        doAutowired();
        //==============MVC部分==============
        //5、初始化HandlerMapping
        doInitHandlerMapping();
    }

    private void doInitHandlerMapping() {
        if(ioc.isEmpty()){return;}

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {

            Class<?> clazz = entry.getValue().getClass();

            if(!clazz.isAnnotationPresent(MyController.class)){continue;}

            String baseUrl = "";
            //这里是拿到类上面配置的访问地址
            if(clazz.isAnnotationPresent(MyRequestMapping.class)){
                MyRequestMapping myRequestMapping = clazz.getAnnotation(MyRequestMapping.class);
                baseUrl = myRequestMapping.value();
            }


            for (Method method : clazz.getMethods()) {

                if(!method.isAnnotationPresent(MyRequestMapping.class)){continue;}

                //这里是拿方法上面的配置的访问地址
                MyRequestMapping myRequestMapping = method.getAnnotation(MyRequestMapping.class);
                String url = ("/"+baseUrl + "/"+myRequestMapping.value()).replaceAll("/+","/");
                //此时就完成了url和方法的匹配
                handlerMapping.put(url,method);
                System.out.println("Mapped :"+url+","+method);

            }

        }

    }

    private void doAutowired() {

        if(ioc.isEmpty()){return;}
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //获取该类所有的属性 包含private public protected default
            for (Field field : entry.getValue().getClass().getDeclaredFields()) {
                //只包含有自定义注解上的属性
                if(!field.isAnnotationPresent(MyAutowired.class)){continue;}

                String beanName = field.getAnnotation(MyAutowired.class).value().trim();
                //如果当前属性 取得名字为""
                //则取你这个属性的类型的名字
                if("".equals(beanName)){
                    beanName = field.getType().getName();
                }

                //暴力访问
                //可以将初始话在ioc容器里面的的值赋予该属性
                field.setAccessible(true);

                try {
                    //此时就完成了自动注入
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }

        }

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
                }else if(clazz.isAnnotationPresent(MyService.class)){
                    String beanName = clazz.getAnnotation(MyService.class).value();
                    if("".equals(beanName.trim())){
                        beanName = toLowerCaseFirst(clazz.getSimpleName());
                    }

                    //2、默认的类名首字母小写
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);

                    //3、如果是接口
                    //判断有多少个实现类，如果只有一个，默认就选择这个实现类
                    //如果有多个，只能抛异常
                    for (Class<?> i : clazz.getInterfaces()) {
                        if(ioc.containsKey(i.getName())){
                            throw new Exception("The " + i.getName() + " is exists!!");
                        }
                        ioc.put(i.getName(),instance);
                    }
                }else {
                    continue;
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
                //将扫描到文件保存起来
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



}
