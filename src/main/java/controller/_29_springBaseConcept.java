package controller;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import service.UserService;

/**
 * @author Erica
 * @date 2020/11/11 22:59
 * @description Spring中的核心概念
 */
public class _29_springBaseConcept {

    public static void main(String[] args) {
        beanDefinitionReaderIntro();
    }

    //概念一：BeanDefinition
    // 在Spring定义bean的方式分为三类，分别是xml文件中的bean标签（<bean/>）、@Bean注解、@Component(@Service,@Controller)
    // 如何通过beanDefinition来定义一个bean呢，如下方法。
    // 总结：1>我们通过<bean/>，@Bean，@Component等方式所定义的Bean，最终都会被解析为BeanDefinition对象。
    //      2>BeanDefinition可以理解为底层源码级别的一个概念，也可以理解为Spring提供的一种API使用的方式。
    private static void beanDefinitionIntro() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition().getBeanDefinition();
        beanDefinition.setBeanClass(UserService.class);// 必须设置项
        beanDefinition.setScope("prototype"); // 非必须设置项：设置作用域
        beanDefinition.setInitMethodName("init"); // 必须设置项：设置初始化方法
        beanDefinition.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE); // 必须设置项：设置自动装配模型
        applicationContext.registerBeanDefinition("userService",beanDefinition);
        applicationContext.refresh();
        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.getInfo();
    }

    //概念二：BeanDefinitionReader(接口)
    // BeanDefinitionReader分为几类：XmlBeanDefinitionReader、AnnotatedBeanDefinitionReader
    private static void beanDefinitionReaderIntro() {
       // 1.XmlBeanDefinitionReader: 可以解析<bean/>标签
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(applicationContext);
        int i = beanDefinitionReader.loadBeanDefinitions("spring.xml");
        /*applicationContext.refresh(); // java.lang.IllegalStateException: org.springframework.context.annotation.AnnotationConfigApplicationContext@887af79 has not been refreshed yet
        System.out.println(i); // 打印结果为1，此时spring.xml中只有一个bean标签
        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.getInfo(); // 123*/
        // 2.AnnotatedBeanDefinitionReader：可以直接把某个类转换为BeanDefinition，并且会解析该类上的注解
        // 它能解析的注解是：@Conditional，@Scope、@Lazy、@Primary、@DependsOn、@Role、@Description
        AnnotatedBeanDefinitionReader annotatedBeanDefinitionReader = new AnnotatedBeanDefinitionReader(applicationContext);
        annotatedBeanDefinitionReader.register(UserService.class);
        applicationContext.refresh();
        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.getInfo(); // 123

    }

}
