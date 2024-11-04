package com.client.controller;

import com.called.service.CalledService;
import com.called.service.CalledService2;
import github.javaguide.annotation.RpcReference;
import github.javaguide.annotation.RpcService;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: ClientController
 * Package: com.client.controller
 */
//@RestController
//@RequestMapping("/client")
@Component
@RestController
public class ClientController {
    //注入service
    @RpcReference(version = "version1",group = "test1")
    CalledService calledService;

    @RpcReference(version = "version2",group = "test2")
    CalledService2 calledService2;

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
