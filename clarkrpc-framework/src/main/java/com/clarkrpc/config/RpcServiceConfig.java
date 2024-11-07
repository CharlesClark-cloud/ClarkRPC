package com.clarkrpc.config;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {
    //服务版本
    private String version = "";
    //一个接口有多个实现的时候 通过版本号以及分组来查找对应的服务
    private String group = "";
    //目标服务
    private Object service;

    public String getRpcServiceName() {
        return this.getServiceName() + this.getGroup() + this.getVersion();
    }

    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
