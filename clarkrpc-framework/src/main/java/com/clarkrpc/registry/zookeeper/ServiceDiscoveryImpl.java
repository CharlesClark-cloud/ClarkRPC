package com.clarkrpc.registry.zookeeper;

import com.clarkrpc.enums.RpcErrorMessageEnum;
import com.clarkrpc.exception.RpcException;
import com.clarkrpc.loadbalance.LoadBalance;
import com.clarkrpc.registry.ServiceDiscovery;
import com.clarkrpc.registry.zookeeper.utils.CuratorUtil;
import com.clarkrpc.remoting.dto.RpcRequest;
import com.clarkrpc.enums.LoadBalanceEnum;
import com.clarkrpc.extension.ExtensionLoader;
import com.clarkrpc.utils.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * ClassName: ServiceDiscoveryImpl
 * Package: clarkrpc.registry.zookeeper
 */
@Slf4j
public class ServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public ServiceDiscoveryImpl() {
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(LoadBalanceEnum.LOADBALANCE.getName());
    }

    @Override
    public InetSocketAddress lookupService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtil.getZkClient();
        List<String> serviceUrlList = CuratorUtil.getChildrenNodes(zkClient, rpcServiceName);
        if (CollectionUtil.isEmpty(serviceUrlList)) {
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND, rpcServiceName);
        }
        // load balancing
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("Successfully found the service address:[{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
