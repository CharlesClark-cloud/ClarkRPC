package com.called.service.impl;

//import com.called.service.CalledService2;
import com.api.client.CalledService2;
import com.clarkrpc.annotation.RpcService;


/**
 * ClassName: CalledServiceImpl2
 * Package: com.called.service.impl
 */
@RpcService(version = "version2",group = "test2")
public class CalledServiceImpl2 implements CalledService2 {
    static {
        System.out.println("测试第二个service");
    }

    @Override
    public String hello(String message) {
        return "第二个service"+message;
    }
}
