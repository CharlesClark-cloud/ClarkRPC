package com.called;

import com.clarkrpc.annotation.RpcScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@RpcScan(basePackage = "com.called")  // 扫描 com.called 包下的服务组件
public class CalledApplication {
    public static void main(String[] args) {
        SpringApplication.run(CalledApplication.class, args);
    }

}
