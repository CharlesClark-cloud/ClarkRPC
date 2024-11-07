package com.clarkrpc.provider;

import com.clarkrpc.config.RpcServiceConfig;

/**
 * ClassName: ServiceProvider
 * Package: clarkrpc.provider
 */

// 存储和提供 服务对象 store and provide service object.
public interface ServiceProvider {
    /**
     * @param rpcServiceConfig rpc service related attributes
     */
    void addService(RpcServiceConfig rpcServiceConfig); // 其中包括服务名称 服务版本号 服务分组

    /**
     * @param rpcServiceName rpc service name
     * @return service object
     */
    Object getService(String rpcServiceName); //通过服务名称获取服务

    /**
     * @param rpcServiceConfig rpc service related attributes
     */
    void publishService(RpcServiceConfig rpcServiceConfig); // 发布服务 通过服务名称 服务版本号 服务分组等信息 发布服务

}
