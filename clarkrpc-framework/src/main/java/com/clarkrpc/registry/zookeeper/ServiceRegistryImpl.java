package com.clarkrpc.registry.zookeeper;

import com.clarkrpc.registry.ServiceRegistry;
import com.clarkrpc.registry.zookeeper.utils.CuratorUtil;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * ClassName: ServiceRegistryImpl
 * Package: clarkrpc.registry.zookeeper
 */
public class ServiceRegistryImpl implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName,InetSocketAddress inetSocketAddress){
        //将服务注册注册到注册中心
        String servicePath = CuratorUtil.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtil.getZkClient();
        CuratorUtil.createPersistentNode(zkClient, servicePath);
    }


}
