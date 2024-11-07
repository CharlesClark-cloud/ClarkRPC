package com.clarkrpc.registry;

import com.clarkrpc.remoting.dto.RpcRequest;
import com.clarkrpc.extension.SPI;

import java.net.InetSocketAddress;

/**
 * ClassName: ServiceDiscovery
 * Package: clarkrpc.registry.zookeeper
 */
@SPI
public interface ServiceDiscovery {

    /**
     * 通过服务名称寻找服务信息
     *
     * @param rpcRequest rpc service pojo
     * @return 服务到地址信息
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);

}
