package com.called.service.impl;


//import com.called.service.CalledService;
import com.api.client.CalledService;
import com.clarkrpc.annotation.RpcService;

/**
 * ClassName: CalledServiceImpl
 * Package: com.called.service.impl
 */
//@RpcService(version = "version1",group = "test1")
@RpcService()
public class CalledServiceImpl implements CalledService {
    static {
        System.out.println("这是我的服务提供类。现在被创建！！！");
    }
    @Override
    public String hello(String message) {
        String messageReturn = "message had been processed：  "+message;
        return messageReturn;
    }
}
