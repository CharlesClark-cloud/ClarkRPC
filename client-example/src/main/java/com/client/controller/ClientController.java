package com.client.controller;


import com.api.client.CalledService;
import com.api.client.CalledService2;
import com.clarkrpc.annotation.RpcReference;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: ClientController
 * Package: com.client.controller
 */
@Component
@RestController
public class ClientController {
    //注入service
    @RpcReference()
    CalledService calledService;

    @RpcReference(version = "version2",group = "test2")
    CalledService2 calledService2;
    @GetMapping("/test")
    public String out3(){
        String hello = calledService.hello("我的消息");

        return hello;
    }

    @GetMapping("/test1")
    public String out(){
        String hello = calledService.hello("我的消息");

        return hello;
    }

    @GetMapping("/test2")
    public String out2(){
        String hello = calledService2.hello("第二个服务我的消息");
        return hello;
    }


//    @GetMapping("/test2")
    public String getServerSupport(){
        String processedMessage = calledService.hello("这是我的消息啊！！！！");
        return processedMessage;
    }


}
