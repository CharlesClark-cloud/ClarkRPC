package com.called.config;

import com.clarkrpc.annotation.RpcScan;
import com.clarkrpc.remoting.transport.netty.server.NettyRpcServer;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NettyServerConfig {

    // 直接依赖 Spring 管理的 NettyRpcServer Bean
    @Autowired
    private NettyRpcServer nettyRpcServer;
    @PostConstruct
    public void init() {
        // 在初始化时启动 NettyRpcServer
        new Thread(() -> {
            try {
                nettyRpcServer.start();  // 启动 Netty 服务器
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}