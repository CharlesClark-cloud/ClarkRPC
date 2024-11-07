package com.clarkrpc.registry;

import com.clarkrpc.extension.SPI;

import java.net.InetSocketAddress;

/**
 * ClassName: ServiceRegistry
 * Package: clarkrpc.registry.zookeeper
 */
@SPI
public interface ServiceRegistry {
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
