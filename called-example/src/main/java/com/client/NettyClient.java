package com.client;

import com.client.controller.ClientController;
import github.javaguide.annotation.RpcScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * ClassName: NettyClient
 * Package: com.client
 */
@RpcScan(basePackage = {"com.client"})
public class NettyClient {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClient.class);
        ClientController clientController = (ClientController) applicationContext.getBean(ClientController.class);
        System.out.println("消息！！！   "+clientController.getServerSupport());
    }
}
