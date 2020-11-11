package controller;

import com.luban.service.LubanOrderService;
import framework.AppConfig;
import framework.LubanAppConfig;
import framework.LubanApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.luban.service.LubanUserService;
import service.UserService;

/**
 * @author Erica
 * @date 2020/11/5 22:31
 * @description 手写模拟Spring框架核心逻辑
 */
public class _28_simulateSpring {

    public static void main1(String[] args) {
        simulationSpringGetBean();
    }

    /**基于xml文件找到指定的bean*/
    private static void getBeanByXml() {
        // 1.定义UserService类，并定义getInfo方法
        // 2.在对应的spring.xml文件中定义bean对象（<bean id="userService" class="service.UserService"/>）
        // 3.通过ClassPathXmlApplicationContext来找到spring.xml文件，找到其中的bean对象，调用其方法
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring.xml");
        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.getInfo(); // 123
    }

    /**基于注解找到指定的bean*/
    private static void getBeanByAnno() {
        // 1.定义UserService类，并定义getInfo方法
        // 2.定义AppConfig类，作为扫描类，需要加注解@ComponentScan("service") ，其中括号里的参数指的是所要扫描到的包路径
        // 3.需要在待扫描的类上加上@Component注解，标识其为一个bean
        // 4.通过CAnnotationConfigApplicationContext来找到AppConfig类，扫描其指定的包，找到其中的bean对象，调用其方法
        // 注：若此时不在UserService类上加上@Component注解的话，运行后的报错信息是：org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named 'userService' available
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.getInfo();
    }

    /** 模拟spring获取bean的方法 */
    private static void simulationSpringGetBean() {
        // 1.无论是以xml的形式还是注解的形式，第一步总是先获取上上下文的信息，并从中取出所需的bean对象。因此，首先要做的就是定义一个上下文类，并定义一个getBean方法；
        // 2.需要定义一个扫描类用于扫描bean对象（LunAppConfig）,因此需要重写ComponentScan注解(LubanComponentScan)，将定义的LubanComponentScan加到LunAppConfig上；
        // 3.需要定义一个生成bean对象的注解@Component,将此注解加到需要生成bean对象的类上；
        // 4.在创建上下文信息的时候，是通过LubanAppConfig类上的LubanComponentScan注解来创建指定路径下的bean对象，如何解析，这是接下来要解决的问题；
        // 5.此时打印出的lubanUserService为null这是因为LubanApplicationContext的getBean方法返回为空，因此需要去编写其内容。
        LubanApplicationContext applicationContext = new LubanApplicationContext(LubanAppConfig.class);
        LubanUserService lubanUserService = (LubanUserService) applicationContext.getBean("lubanUserService");
        lubanUserService.getOrderInfo(); // 调用LubanOrderService中的getOrderInfo方法
        // 6.获取到所定义bean的名称
        LubanOrderService lubanOrderService = (LubanOrderService)applicationContext.getBean("lubanOrderService");
        lubanOrderService.getBeanName(); // null
        // 6-1 在LubanApplicationContext中创建bean的方法doCreateBean中通过spring容器来获取到所定义的bean的名称
        lubanOrderService.getBeanName(); // lubanOrderService
        // 7.对所创建的bean进行初始化
        // 8.前置处理器BeanPostProcessor的处理
    }

}
