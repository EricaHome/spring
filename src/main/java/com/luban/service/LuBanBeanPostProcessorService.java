package com.luban.service;

import framework.LuBanBeanPostProcessor;
import framework.LubanComponent;

@LubanComponent
public class LuBanBeanPostProcessorService implements LuBanBeanPostProcessor {
    @Override
    public void postProcessBeforeInitialization(String beanName, Object bean) {
        System.out.println("postProcessBeforeInitialization");
    }

    @Override
    public void postProcessAfterInitialization(String beanName, Object bean) {
        System.out.println("postProcessAfterInitialization");
    }
}
