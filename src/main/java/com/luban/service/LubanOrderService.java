package com.luban.service;

import framework.BeanNameAware;
import framework.LubanComponent;
import framework.LubanInitializingBean;

@LubanComponent("lubanOrderService")
public class LubanOrderService implements BeanNameAware, LubanInitializingBean {

    private String beanName;

    public void getOrderInfo() {
        System.out.println("调用LubanOrderService中的getOrderInfo方法");
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void getBeanName() {
        System.out.println(this.beanName);
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("bean生成后的初始化方法");
    }
}
