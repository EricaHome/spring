package com.luban.service;

import framework.LubanAutowired;
import framework.LubanComponent;

/**
 * @author Erica
 * @date 2020/11/5 22:34
 * @description TODO
 */
@LubanComponent("lubanUserService")
public class LubanUserService {

    @LubanAutowired
    private LubanOrderService lubanOrderService;

    public void getInfo() {
        System.out.println("123");
    }

    public void getOrderInfo() {
        lubanOrderService.getOrderInfo();
    }

}
