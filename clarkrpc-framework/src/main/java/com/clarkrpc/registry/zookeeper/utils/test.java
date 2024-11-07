package com.clarkrpc.registry.zookeeper.utils;

import org.apache.curator.framework.CuratorFramework;

import java.util.List;

/**
 * ClassName: test
 * Package: com.clarkrpc.registry.zookeeper.utils
 */
public class test {
    public static void main(String[] args) {

        String rpcServiceName = "com.called.service.CalledService2test2version2";
        CuratorFramework zkClient = CuratorUtil.getZkClient();
        List<String> serviceUrlList = CuratorUtil.getChildrenNodes(zkClient, rpcServiceName);
        System.out.println(serviceUrlList.size());

    }
}
