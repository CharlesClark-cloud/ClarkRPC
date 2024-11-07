package com.clarkrpc.provider.impl;

import com.clarkrpc.config.RpcServiceConfig;
import com.clarkrpc.provider.ServiceProvider;
import com.clarkrpc.registry.ServiceRegistry;
import com.clarkrpc.remoting.transport.netty.server.NettyRpcServer;
import com.clarkrpc.enums.RpcErrorMessageEnum;
import com.clarkrpc.enums.ServiceRegistryEnum;
import com.clarkrpc.exception.RpcException;
import com.clarkrpc.extension.ExtensionLoader;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassName: ZkServiceProviderImpl
 * Package: clarkrpc.provider.impl
 */
//通过连接zk，实现通过zk将服务与注册中心建立连接
@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {
    /**
     * key: rpc service name(interface name + version + group)
     * value: service object
     */
    private final Map<String, Object> serviceMap; //服务记录map
    private final Set<String> registeredService;  //以及注册过的服务集合
    private final ServiceRegistry serviceRegistry; //用于服务注册 已经在zk模块进行了接口实现
    public ZkServiceProviderImpl(){
        serviceMap = new ConcurrentHashMap<>();
        registeredService = ConcurrentHashMap.newKeySet();
        serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(ServiceRegistryEnum.ZK.getName());
    }
    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if(registeredService.contains(rpcServiceName)){
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName, rpcServiceConfig.getService());
        log.info("Add service: {} and interfaces:{}", rpcServiceName, rpcServiceConfig.getService().getClass().getInterfaces());

    }

    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if (null == service) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return service;
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig);
            serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), new InetSocketAddress(host, NettyRpcServer.PORT));
        } catch (UnknownHostException e) {
            log.error("occur exception when getHostAddress", e);
        }
    }
}
